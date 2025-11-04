import datetime as _dt
import random
import string
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

# Load environment variables
JWT_SECRET = os.getenv("JWT_SECRET", "your-secret-key-here")
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

async def get_user_by_email(email: str, db: _orm.Session):
    return db.query(_models.User).filter(_models.User.email == email).first()

async def create_user(user: _schemas.UserCreate, db: _orm.Session):
    hashed_password = _hash.bcrypt.hash(user.password)
    db_user = _models.User(
        name=user.name, email=user.email, hashed_password=hashed_password, role=(user.role or "user")
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

async def authenticate_user(email: str, password: str, db: _orm.Session):
    user = await get_user_by_email(email=email, db=db)
    if not user:
        return False
    if not user.verify_password(password):
        return False
    if not user.is_verified:
        return "is_verified_false"
    return user


async def create_token(user: _models.User):
    user_obj = _schemas.User.from_orm(user)
    claims = {
        "id": user_obj.id,
        "name": user_obj.name,
        "email": user_obj.email,
        "role": user_obj.role,
        "exp": _dt.datetime.utcnow() + _dt.timedelta(minutes=30),
    }
    return jwt.encode(claims, JWT_SECRET, algorithm="HS256")

async def get_current_user(
    db: _orm.Session = Depends(get_db), token: str = Depends(oauth2_scheme)
):
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user = db.query(_models.User).get(payload["id"])
    except Exception:
        return None
    return _schemas.User.from_orm(user)

def generate_otp():
    # Generate a random OTP
    return str(random.randint(100000, 999999))

def connect_to_rabbitmq():
    # Connect to RabbitMQ
    while True:
        try:
            credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
            connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST, credentials=credentials))
            return connection
        except pika.exceptions.AMQPConnectionError:
            print("Failed to connect to RabbitMQ. Retrying in 5 seconds...")
            time.sleep(5)


def send_otp(email, otp, channel):
    # Send an OTP email notification using RabbitMQ
    connection = connect_to_rabbitmq()
    channel = connection.channel()
    message = {'email': email,
               'subject': 'Account Verification OTP Notification',
               'other': 'null',
               'body': f'Your OTP for account verification is: {otp} \n Please enter this OTP on the verification page to complete your account setup. \n If you did not request this OTP, please ignore this message.\n Thank you '
                }

    try:
        queue_declare_ok = channel.queue_declare(queue='email_notification', passive=True)
        current_durable = queue_declare_ok.method.queue

        if current_durable:
            if queue_declare_ok.method.queue != current_durable:
                channel.queue_delete(queue='email_notification')
                channel.queue_declare(queue='email_notification', durable=True)
        else:
            channel.queue_declare(queue='email_notification', durable=True)

        channel.basic_publish(
            exchange="",
            routing_key='email_notification',
            body=json.dumps(message),
            properties=pika.BasicProperties(
                delivery_mode=pika.spec.PERSISTENT_DELIVERY_MODE
            ),
        )
        print("Sent OTP email notification")
    except Exception as err:
        print(f"Failed to publish message: {err}")
    finally:
        channel.close()
        connection.close()