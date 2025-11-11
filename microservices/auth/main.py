from typing import List
from fastapi import HTTPException
import fastapi as _fastapi
import schemas as _schemas
import sqlalchemy.orm as _orm
import models as _models
import service as _services
import logging
import database as _database
import pika
import os

app = _fastapi.FastAPI()
logging.basicConfig(level=logging.INFO)

rabbit_host = os.environ.get("RABBITMQ_HOST", "some-rabbit")
rabbit_user = os.environ.get("RABBITMQ_DEFAULT_USER", "guest")
rabbit_pass = os.environ.get("RABBITMQ_DEFAULT_PASS", "guest")
channel = None

@app.on_event("startup")
async def startup_event():
    global channel
    try:
        credentials = pika.PlainCredentials(rabbit_user, rabbit_pass)
        connection = pika.BlockingConnection(pika.ConnectionParameters(host=rabbit_host, credentials=credentials))
        channel = connection.channel()
        channel.queue_declare(queue='email_notification', durable=True)
        logging.info(f"RabbitMQ connection established successfully to {rabbit_host} as {rabbit_user}")
    except Exception as e:
        logging.error(f"Failed to connect to RabbitMQ: {e}")

@app.post("/api/users", tags=['User Auth'])
async def create_user(user: _schemas.UserCreate, db: _orm.Session = _fastapi.Depends(_services.get_db)):
    db_user = await _services.get_user_by_email(email=user.n_user_email, db=db)
    if db_user:
        raise _fastapi.HTTPException(status_code=400, detail="User with that email already exists")
    # Check cedula uniqueness
    existing_cedula = db.query(_models.User).filter(_models.User.k_user_cc == user.k_user_cc).first()
    if existing_cedula:
        raise _fastapi.HTTPException(status_code=400, detail="Cedula already registered")
    created = await _services.create_user(user=user, db=db)
    return {"detail": "User Registered, Please verify email to activate account !", "k_user_cc": created.k_user_cc}

@app.get("/check_api")
async def check_api():
    return {"status": "Connected to API Successfully"}

@app.post("/api/token", tags=['User Auth'])
async def generate_token(user_data: _schemas.GenerateUserToken, db: _orm.Session = _fastapi.Depends(_services.get_db)):
    user = await _services.authenticate_user(email_or_username=user_data.username, password=user_data.password, db=db)
    if user == "is_verified_false":
        raise _fastapi.HTTPException(
            status_code=403,
            detail={"message": "Email verification is pending. Please verify your email to proceed.", "is_verified": False, "code": "EMAIL_NOT_VERIFIED"}
        )
    if not user:
        raise _fastapi.HTTPException(status_code=401, detail={"message": "Invalid Credentials", "is_verified": False, "code": "INVALID_CREDENTIALS"})
    token = await _services.create_token(user=user)
    expires_in = int(os.environ.get("TOKEN_EXP_MINUTES", "30")) * 60
    return {"access_token": token, "token_type": "bearer", "is_verified": True, "expires_in": expires_in}

@app.get("/api/users/me", response_model=_schemas.UserBase, tags=['User Auth'])
async def get_current_user(user: _schemas.UserBase = _fastapi.Depends(_services.get_current_user)):
    if not user:
        raise _fastapi.HTTPException(status_code=401, detail="Not authenticated")
    return user

@app.post("/api/users/generate_otp", response_model=str, tags=["User Auth"]) 
async def send_otp_mail(userdata: _schemas.GenerateOtp, db: _orm.Session = _fastapi.Depends(_services.get_db)):
    user = await _services.get_user_by_email(email=userdata.email, db=db)
    if not user:
        raise _fastapi.HTTPException(status_code=404, detail={"message": "User not found", "code": "USER_NOT_FOUND"})
    if user.t_is_verified:
        raise _fastapi.HTTPException(status_code=400, detail={"message": "User is already verified", "code": "ALREADY_VERIFIED"})
    otp = _services.generate_otp()
    _services.send_otp(userdata.email, otp, channel)
    await _services.create_email_verification(db, user, otp)
    return "OTP sent to your email"

@app.post("/api/users/verify_otp", tags=["User Auth"], response_model=str)
async def verify_otp(userdata: _schemas.VerifyOtp, db: _orm.Session = _fastapi.Depends(_services.get_db)):
    user = await _services.get_user_by_email(email=userdata.email, db=db)
    if not user:
        raise _fastapi.HTTPException(status_code=404, detail={"message": "User not found", "code": "USER_NOT_FOUND"})
    ok = await _services.verify_email_otp(db, userdata.email, userdata.otp)
    if not ok:
        raise _fastapi.HTTPException(status_code=400, detail={"message": "Invalid or expired OTP", "code": "INVALID_OTP"})
    user.t_is_verified = True
    db.add(user)
    db.commit()
    return "Email verified successfully"

@app.get("/api/users/verify_email/{email}", tags=["User Auth"], response_model=str)
async def verify_email(email: str, db: _orm.Session = _fastapi.Depends(_services.get_db)):
    user = await _services.get_user_by_email(email=email, db=db)
    if not user:
        raise _fastapi.HTTPException(status_code=404, detail={"message": "User not found", "code": "USER_NOT_FOUND"})
    if user.t_is_verified:
        return "Email is already verified"
    return "Email is not verified"

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=5000, reload=True)