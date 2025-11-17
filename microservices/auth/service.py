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
import httpx

USERS_SERVICE_URL = os.getenv("USERS_SERVICE_URL", "http://users-service:5003")

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

# --- User data via users-service ---
async def get_user_by_email(email: str):
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            r = await client.get(f"{USERS_SERVICE_URL}/api/users/public/by-email/{email}")
            if r.status_code == 200:
                return r.json()
    except Exception:
        return None
    return None

async def create_user_remote(user: _schemas.UserCreate):
    async with httpx.AsyncClient(timeout=10) as client:
        r = await client.post(f"{USERS_SERVICE_URL}/api/users", json={
            "k_user_cc": user.k_user_cc,
            "n_username": user.n_username,
            "password": user.password,
            "n_user_first_name": user.n_user_first_name,
            "n_user_second_name": user.n_user_second_name,
            "n_user_first_lastname": user.n_user_first_lastname,
            "n_user_second_lastname": user.n_user_second_lastname,
            "f_user_birthdate": str(user.f_user_birthdate),
            "n_user_email": user.n_user_email
        })
        if r.status_code not in (200, 201):
            raise ValueError(r.text)
        return r.json()

async def authenticate_user(email_or_username: str, password: str, db: _orm.Session):
    async with httpx.AsyncClient(timeout=5) as client:
        r = await client.post(f"{USERS_SERVICE_URL}/api/users/public/verify", params={"identifier": email_or_username, "password": password})
    if r.status_code != 200:
        return False
    data = r.json()
    if not data.get("t_is_verified"):
        return "is_verified_false"
    return data  # return dict

# --- Token utilities ---
async def create_token(user: dict):
    claims = {
        "k_user_cc": user["k_user_cc"],
        "n_username": user["n_username"],
        "n_user_email": user["n_user_email"],
        "exp": _dt.datetime.utcnow() + _dt.timedelta(minutes=TOKEN_EXP_MINUTES),
        "iat": _dt.datetime.utcnow(),
        "nbf": _dt.datetime.utcnow(),
        "iss": "auth-service",
    }
    if JWT_ALGORITHM == "RS256" and SIGNING_KEY:
        return jwt.encode(claims, SIGNING_KEY, algorithm="RS256")
    return jwt.encode(claims, JWT_SECRET, algorithm="HS256")

async def get_current_user(token: str = Depends(oauth2_scheme)):
    try:
        if JWT_ALGORITHM == "RS256" and VERIFY_KEY:
            payload = jwt.decode(token, VERIFY_KEY, algorithms=["RS256"], issuer="auth-service")
        else:
            payload = jwt.decode(token, JWT_SECRET, algorithms=["HS256"], issuer="auth-service")
        email = payload.get("n_user_email")
    except Exception:
        return None
    data = await get_user_by_email(email)
    if not data:
        return None
    from pydantic import ValidationError
    try:
        return _schemas.UserBase.model_validate(data)
    except ValidationError:
        return None

# --- OTP utilities (local email_verification table only) ---

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

async def create_email_verification(db: _orm.Session, user_data: dict, otp: str):
    otp_hash = _hash.bcrypt.hash(otp)
    record = _models.EmailVerification(
        n_otp_hash=otp_hash,
        n_user_email=user_data["n_user_email"],
        f_expires_at=_dt.datetime.utcnow() + _dt.timedelta(minutes=OTP_EXP_MINUTES),
        t_consumed=False,
        f_created_at=_dt.datetime.utcnow(),
        k_user_cc=user_data["k_user_cc"],
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
    record.t_consumed = True
    db.add(record)
    return True

async def verify_and_mark_email(db: _orm.Session, email: str, otp: str):
    ok = await verify_email_otp(db, email, otp)
    if not ok:
        return False
    async with httpx.AsyncClient(timeout=5) as client:
        r = await client.post(f"{USERS_SERVICE_URL}/api/users/public/mark-verified", params={"email": email})
        if r.status_code != 200:
            return False
    return True