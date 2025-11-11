import datetime as _dt
import random
import jwt
from fastapi import Depends
from fastapi.security import OAuth2PasswordBearer
import sqlalchemy.orm as _orm
import passlib.hash as _hash
import models as _models
import schemas as _schemas
import database as _database
import os
import json
import pika
import time

JWT_SECRET = os.getenv("JWT_SECRET", "your-secret-key-here")
TOKEN_EXP_MINUTES = int(os.getenv("TOKEN_EXP_MINUTES", "30"))
OTP_EXP_MINUTES = int(os.getenv("OTP_EXP_MINUTES", "10"))
JWT_PRIVATE_KEY_PATH = os.getenv("JWT_PRIVATE_KEY_PATH", "/run/secrets/jwt_private.pem")
JWT_PUBLIC_KEY_PATH = os.getenv("JWT_PUBLIC_KEY_PATH", "/run/secrets/jwt_public.pem")
JWT_PRIVATE_KEY_INLINE = os.getenv("JWT_PRIVATE_KEY")
JWT_PUBLIC_KEY_INLINE = os.getenv("JWT_PUBLIC_KEY")

SIGNING_KEY = None
VERIFY_KEY = None
JWT_ALGORITHM = "HS256"

try:
    if JWT_PRIVATE_KEY_INLINE:
        SIGNING_KEY = JWT_PRIVATE_KEY_INLINE
    elif JWT_PRIVATE_KEY_PATH and os.path.exists(JWT_PRIVATE_KEY_PATH):
        with open(JWT_PRIVATE_KEY_PATH, "r") as f:
            SIGNING_KEY = f.read()
    if JWT_PUBLIC_KEY_INLINE:
        VERIFY_KEY = JWT_PUBLIC_KEY_INLINE
    elif JWT_PUBLIC_KEY_PATH and os.path.exists(JWT_PUBLIC_KEY_PATH):
        with open(JWT_PUBLIC_KEY_PATH, "r") as f:
            VERIFY_KEY = f.read()
    if SIGNING_KEY and VERIFY_KEY:
        JWT_ALGORITHM = "RS256"
except Exception:
    SIGNING_KEY = None
    VERIFY_KEY = None
    JWT_ALGORITHM = "HS256"

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "some-rabbit")
RABBITMQ_USER = os.getenv("RABBITMQ_DEFAULT_USER", "guest")
RABBITMQ_PASS = os.getenv("RABBITMQ_DEFAULT_PASS", "guest")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/token")


def get_db():
    db = _database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

# Get by email
async def get_user_by_email(email: str, db: _orm.Session):
    return db.query(_models.User).filter(_models.User.n_user_email == email).first()

# Get by username
async def get_user_by_username(username: str, db: _orm.Session):
    return db.query(_models.User).filter(_models.User.n_username == username).first()

async def create_user(user: _schemas.UserCreate, db: _orm.Session):
    hashed_password = _hash.bcrypt.hash(user.password)
    db_user = _models.User(
        k_user_cc=user.k_user_cc,  # cedula provided by client
        n_username=user.n_username,
        n_hashed_password=hashed_password,
        n_user_first_name=user.n_user_first_name,
        n_user_second_name=user.n_user_second_name,
        n_user_first_lastname=user.n_user_first_lastname,
        n_user_second_lastname=user.n_user_second_lastname,
        f_user_birthdate=user.f_user_birthdate,
        n_user_email=user.n_user_email,
        t_subscription_type="NONE",
        f_user_registration_date=_dt.datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S"),
        t_is_verified=False,
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

async def authenticate_user(email_or_username: str, password: str, db: _orm.Session):
    user = await get_user_by_email(email=email_or_username, db=db)
    if not user:
        user = await get_user_by_username(username=email_or_username, db=db)
    if not user:
        return False
    if not user.verify_password(password):
        return False
    if not user.t_is_verified:
        return "is_verified_false"
    return user

async def create_token(user: _models.User):
    claims = {
        "k_user_cc": user.k_user_cc,
        "n_username": user.n_username,
        "n_user_email": user.n_user_email,
        "exp": _dt.datetime.utcnow() + _dt.timedelta(minutes=TOKEN_EXP_MINUTES),
        "iat": _dt.datetime.utcnow(),
        "nbf": _dt.datetime.utcnow(),
        "iss": "auth-service",
    }
    if JWT_ALGORITHM == "RS256" and SIGNING_KEY:
        return jwt.encode(claims, SIGNING_KEY, algorithm="RS256")
    return jwt.encode(claims, JWT_SECRET, algorithm="HS256")

async def get_current_user(db: _orm.Session = Depends(get_db), token: str = Depends(oauth2_scheme)):
    try:
        if JWT_ALGORITHM == "RS256" and VERIFY_KEY:
            payload = jwt.decode(token, VERIFY_KEY, algorithms=["RS256"], issuer="auth-service")
        else:
            payload = jwt.decode(token, JWT_SECRET, algorithms=["HS256"], issuer="auth-service")
        user = db.query(_models.User).get(payload["k_user_cc"])
    except Exception:
        return None
    from pydantic import ValidationError
    try:
        return _schemas.UserBase.model_validate(user)
    except ValidationError:
        return None

# OTP utilities

def generate_otp():
    return str(random.randint(100000, 999999))

def connect_to_rabbitmq():
    while True:
        try:
            credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
            connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST, credentials=credentials))
            return connection
        except pika.exceptions.AMQPConnectionError:
            print("Failed to connect to RabbitMQ. Retrying in 5 seconds...")
            time.sleep(5)


def send_otp(email, otp, channel):
    connection = connect_to_rabbitmq()
    channel = connection.channel()
    message = {
        'email': email,
        'subject': 'Account Verification OTP Notification',
        'other': 'null',
        'body': f'Your OTP for account verification is: {otp} \n Please enter this OTP on the verification page to complete your account setup. \n If you did not request this OTP, please ignore this message.'
    }
    try:
        channel.queue_declare(queue='email_notification', durable=True)
        channel.basic_publish(
            exchange="",
            routing_key='email_notification',
            body=json.dumps(message),
            properties=pika.BasicProperties(delivery_mode=pika.spec.PERSISTENT_DELIVERY_MODE),
        )
    except Exception as err:
        print(f"Failed to publish message: {err}")
    finally:
        channel.close()
        connection.close()

async def create_email_verification(db: _orm.Session, user: _models.User, otp: str):
    otp_hash = _hash.bcrypt.hash(otp)
    record = _models.EmailVerification(
        n_otp_hash=otp_hash,
        n_user_email=user.n_user_email,
        f_expires_at=_dt.datetime.utcnow() + _dt.timedelta(minutes=OTP_EXP_MINUTES),
        t_consumed=False,
        f_created_at=_dt.datetime.utcnow(),
        k_user_cc=user.k_user_cc,
    )
    db.add(record)
    db.commit()
    db.refresh(record)
    return record

async def verify_email_otp(db: _orm.Session, email: str, otp: str):
    q = db.query(_models.EmailVerification).filter(
        _models.EmailVerification.n_user_email == email,
        _models.EmailVerification.t_consumed == False,
    ).order_by(_models.EmailVerification.f_created_at.desc())
    record = q.first()
    if not record:
        return False
    if record.f_expires_at < _dt.datetime.utcnow():
        return False
    if not _hash.bcrypt.verify(otp, record.n_otp_hash):
        return False
    # mark consumed
    record.t_consumed = True
    db.add(record)
    return True