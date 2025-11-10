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

# Load environment variables
# Backward-compatible secret (HS256 fallback)
JWT_SECRET = os.getenv("JWT_SECRET", "your-secret-key-here")
TOKEN_EXP_MINUTES = int(os.getenv("TOKEN_EXP_MINUTES", "30"))

# RS256 key configuration
JWT_PRIVATE_KEY_PATH = os.getenv("JWT_PRIVATE_KEY_PATH", "/run/secrets/jwt_private.pem")
JWT_PUBLIC_KEY_PATH = os.getenv("JWT_PUBLIC_KEY_PATH", "/run/secrets/jwt_public.pem")
JWT_PRIVATE_KEY_INLINE = os.getenv("JWT_PRIVATE_KEY")  # optional direct PEM content
JWT_PUBLIC_KEY_INLINE = os.getenv("JWT_PUBLIC_KEY")    # optional direct PEM content

SIGNING_KEY = None
VERIFY_KEY = None
JWT_ALGORITHM = "HS256"

# Try to load RSA keys if available
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
    # If any error reading keys, keep HS256 fallback
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

# Obtener usuario por correo
async def get_user_by_email(email: str, db: _orm.Session):
    return db.query(_models.User).filter(_models.User.n_correo_electronico == email).first()

# Obtener usuario por nombre de usuario
async def get_user_by_username(username: str, db: _orm.Session):
    return db.query(_models.User).filter(_models.User.n_usuario == username).first()

async def create_user(user: _schemas.UserCreate, db: _orm.Session):
    hashed_password = _hash.bcrypt.hash(user.password)
    db_user = _models.User(
        n_usuario=user.n_usuario,
        n_contrasena="__legacy__",  # placeholder para columna obligatoria
        hashed_password=hashed_password,
        n_primer_nombre=user.n_primer_nombre,
        n_segundo_nombre=user.n_segundo_nombre,
        n_primer_apellido=user.n_primer_apellido,
        n_segundo_apellido=user.n_segundo_apellido,
        f_fecha_nacimiento=user.f_fecha_nacimiento,
        n_correo_electronico=user.n_correo_electronico,
        t_tipo_suscripcion="NINGUNA",
        f_fecha_de_registro=_dt.datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S"),
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

async def authenticate_user(email: str, password: str, db: _orm.Session):
    # Permitir login por correo o nombre de usuario
    user = await get_user_by_email(email=email, db=db)
    if not user:
        user = await get_user_by_username(username=email, db=db)
    if not user:
        return False
    if not user.verify_password(password):
        return False
    if not user.is_verified:
        return "is_verified_false"
    return user

# Obtener roles del usuario
async def get_user_roles(db: _orm.Session, user_id: int) -> list[str]:
    rows = db.query(_models.Rol.n_nombre).join(
        _models.UsuarioRol, _models.Rol.k_id_rol == _models.UsuarioRol.k_id_rol
    ).filter(_models.UsuarioRol.k_cedula_ciudadania_usuario == user_id).all()
    return [r[0] for r in rows] if rows else []

async def create_token(user: _models.User, db: _orm.Session):
    roles = await get_user_roles(db, user.k_cedula_ciudadania_usuario)
    claims = {
        "k_cedula_ciudadania_usuario": user.k_cedula_ciudadania_usuario,
        "n_usuario": user.n_usuario,
        "n_correo_electronico": user.n_correo_electronico,
        "roles": roles or ["USER"],
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
        user = db.query(_models.User).get(payload["k_cedula_ciudadania_usuario"])
    except Exception:
        return None
    # Retornar esquema Pydantic (UserBase) para compatibilidad con response_model
    from pydantic import ValidationError
    try:
        return _schemas.UserBase.model_validate(user)
    except ValidationError:
        return None

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
        'body': f'Your OTP for account verification is: {otp} \n Please enter this OTP on the verification page to complete your account setup. \n If you did not request this OTP, please ignore this message.\n Thank you '
    }

    try:
        channel.queue_declare(queue='email_notification', durable=True)
        channel.basic_publish(
            exchange="",
            routing_key='email_notification',
            body=json.dumps(message),
            properties=pika.BasicProperties(delivery_mode=pika.spec.PERSISTENT_DELIVERY_MODE),
        )
        print("Sent OTP email notification")
    except Exception as err:
        print(f"Failed to publish message: {err}")
    finally:
        channel.close()
        connection.close()

async def user_has_role(db: _orm.Session, user_id: int, roles: list[str]) -> bool:
    return db.query(_models.Rol.n_nombre).join(_models.UsuarioRol, _models.Rol.k_id_rol == _models.UsuarioRol.k_id_rol)\
        .filter(_models.UsuarioRol.k_cedula_ciudadania_usuario == user_id, _models.Rol.n_nombre.in_(roles)).first() is not None

from fastapi import HTTPException
async def require_roles(roles: list[str], db: _orm.Session = Depends(get_db), token: str = Depends(oauth2_scheme)):
    # Obtener usuario actual
    current = await get_current_user(db=db, token=token)
    if not current:
        raise HTTPException(status_code=401, detail={"message": "Not authenticated", "code": "NOT_AUTHENTICATED"})
    # current es UserBase, necesitamos ID
    user = db.query(_models.User).get(current.k_cedula_ciudadania_usuario)
    if not user or not await user_has_role(db, user.k_cedula_ciudadania_usuario, roles):
        raise HTTPException(status_code=403, detail={"message": "Insufficient role", "code": "INSUFFICIENT_ROLE"})
    return user