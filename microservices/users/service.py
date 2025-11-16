import sqlalchemy.orm as _orm
import models as _models
import schemas as _schemas
import datetime as _dt
import passlib.hash as _hash
import os
import database as _database

# Dependency

def get_db():
    db = _database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

async def get_user(db: _orm.Session, user_id: int):
    return db.query(_models.User).filter(_models.User.k_user_cc == user_id).first()

async def get_user_by_email(db: _orm.Session, email: str):
    return db.query(_models.User).filter(_models.User.n_user_email == email).first()

async def get_user_by_username(db: _orm.Session, username: str):
    return db.query(_models.User).filter(_models.User.n_username == username).first()

async def list_users(db: _orm.Session, skip: int = 0, limit: int = 50):
    return db.query(_models.User).offset(skip).limit(limit).all()

async def create_user(db: _orm.Session, user: _schemas.UserCreate):
    hashed_password = _hash.bcrypt.hash(user.password)
    db_user = _models.User(
        k_user_cc=user.k_user_cc,
        n_username=user.n_username,
        n_hashed_password=hashed_password,
        n_user_first_name=user.n_user_first_name,
        n_user_second_name=user.n_user_second_name,
        n_user_first_lastname=user.n_user_first_lastname,
        n_user_second_lastname=user.n_user_second_lastname,
        f_user_birthdate=user.f_user_birthdate,
        n_user_email=user.n_user_email,
        t_subscription_type="NONE",
        f_user_registration_date=_dt.date.today(),
        t_is_verified=False,
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

async def update_user(db: _orm.Session, user: _models.User, data: _schemas.UserUpdate):
    if data.n_user_first_name is not None:
        user.n_user_first_name = data.n_user_first_name
    if data.n_user_second_name is not None:
        user.n_user_second_name = data.n_user_second_name
    if data.n_user_first_lastname is not None:
        user.n_user_first_lastname = data.n_user_first_lastname
    if data.n_user_second_lastname is not None:
        user.n_user_second_lastname = data.n_user_second_lastname
    if data.f_user_birthdate is not None:
        user.f_user_birthdate = data.f_user_birthdate
    if data.t_subscription_type is not None:
        user.t_subscription_type = data.t_subscription_type
    if getattr(data, "t_is_verified", None) is not None:
        user.t_is_verified = data.t_is_verified
    db.add(user)
    db.commit()
    db.refresh(user)
    return user

async def delete_user(db: _orm.Session, user: _models.User):
    db.delete(user)
    db.commit()
    return True

# Auth-service helpers (no direct token logic here)
async def verify_credentials(db: _orm.Session, identifier: str, password: str):
    user = await get_user_by_email(db, identifier)
    if not user:
        user = await get_user_by_username(db, identifier)
    if not user:
        return None
    if not _hash.bcrypt.verify(password, user.n_hashed_password):
        return None
    return user

async def mark_user_verified(db: _orm.Session, email: str):
    user = await get_user_by_email(db, email)
    if not user:
        return None
    user.t_is_verified = True
    db.add(user)
    db.commit()
    db.refresh(user)
    return user
