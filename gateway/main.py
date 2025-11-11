from fastapi import FastAPI, HTTPException, File, UploadFile
import fastapi as _fastapi
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from dotenv import load_dotenv
from jwt.exceptions import DecodeError, ExpiredSignatureError, InvalidIssuerError
import requests
import base64
import pika
import logging
import os
import jwt
import rpc_client
import datetime as _dt

# Import Pydantic schemas
from schemas import UserCredentials, UserRegisteration, GenerateOtp, VerifyOtp, BicycleBase

app = FastAPI()
security = HTTPBearer()

# Load environment variables
load_dotenv()
logging.basicConfig(level=logging.INFO)

# Retrieve environment variables
JWT_SECRET = os.environ.get("JWT_SECRET")
AUTH_BASE_URL = os.environ.get("AUTH_BASE_URL")
RABBITMQ_URL = os.environ.get("RABBITMQ_URL", "rabbitmq")
RABBITMQ_USER = os.environ.get("RABBITMQ_DEFAULT_USER", "guest")
RABBITMQ_PASS = os.environ.get("RABBITMQ_DEFAULT_PASS", "guest")
BICYCLE_SERVICE_URL = os.environ.get("BICYCLE_SERVICE_URL")

# Optional RS256 public key support
JWT_PUBLIC_KEY_PATH = os.environ.get("JWT_PUBLIC_KEY_PATH", "/run/secrets/jwt_public.pem")
JWT_PUBLIC_KEY_INLINE = os.environ.get("JWT_PUBLIC_KEY")
VERIFY_KEY = None
JWT_ALGORITHM = "HS256"
try:
    if JWT_PUBLIC_KEY_INLINE:
        VERIFY_KEY = JWT_PUBLIC_KEY_INLINE
        JWT_ALGORITHM = "RS256"
    elif JWT_PUBLIC_KEY_PATH and os.path.exists(JWT_PUBLIC_KEY_PATH):
        with open(JWT_PUBLIC_KEY_PATH, "r") as f:
            VERIFY_KEY = f.read()
            JWT_ALGORITHM = "RS256"
except Exception:
    VERIFY_KEY = None
    JWT_ALGORITHM = "HS256"

# Global variable for RabbitMQ connection
channel = None

@app.on_event("startup")
async def startup_event():
    """Initialize RabbitMQ connection on startup"""
    global channel
    try:
        credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
        connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_URL, credentials=credentials))
        channel = connection.channel()
        channel.queue_declare(queue='gatewayservice')
        channel.queue_declare(queue='ocr_service')
        logging.info(f"Gateway: RabbitMQ connection established successfully to {RABBITMQ_URL} as {RABBITMQ_USER}")
    except Exception as e:
        logging.error(f"Gateway: Failed to connect to RabbitMQ: {e}")


# JWT token validation
async def jwt_validation(credentials: HTTPAuthorizationCredentials = _fastapi.Depends(security)):
    try:
        token = credentials.credentials
        if JWT_ALGORITHM == "RS256" and VERIFY_KEY:
            payload = jwt.decode(token, VERIFY_KEY, algorithms=["RS256"], issuer="auth-service")
        else:
            payload = jwt.decode(token, JWT_SECRET, algorithms=["HS256"], issuer="auth-service")
        return payload
    except ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token expired")
    except (DecodeError, InvalidIssuerError):
        raise HTTPException(status_code=401, detail="Invalid JWT token")


# Authentication routes
@app.post("/auth/login", tags=['Authentication Service'])
async def login(user_data: UserCredentials):
    try:
        response = requests.post(f"{AUTH_BASE_URL}/api/token", json={"username": user_data.username, "password": user_data.password})
        data = response.json()
        if response.status_code == 200:
            # Normalizar respuesta de éxito
            return {
                "access_token": data.get("access_token"),
                "token_type": data.get("token_type", "bearer"),
                "is_verified": data.get("is_verified", False),
                "expires_in": data.get("expires_in")
            }
        else:
            # Error normalizado
            detail = data.get("detail", data)
            if isinstance(detail, dict):
                raise HTTPException(status_code=response.status_code, detail=detail)
            raise HTTPException(status_code=response.status_code, detail={"message": str(detail), "code": "AUTH_ERROR"})
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail={"message": "Authentication service is unavailable", "code": "AUTH_UNAVAILABLE"})

@app.post("/auth/register", tags=['Authentication Service'])
async def registeration(user_data: UserRegisteration):
    try:
        # Ensure birthdate is JSON-serializable string
        bd = user_data.f_user_birthdate
        if hasattr(bd, "isoformat"):
            bd_str = bd.isoformat()
        else:
            bd_str = str(bd)
        payload = {
            "k_user_cc": user_data.k_user_cc,
            "n_username": user_data.n_username,
            "password": user_data.password,
            "n_user_first_name": user_data.n_user_first_name,
            "n_user_second_name": user_data.n_user_second_name,
            "n_user_first_lastname": user_data.n_user_first_lastname,
            "n_user_second_lastname": user_data.n_user_second_lastname,
            "f_user_birthdate": bd_str,
            "n_user_email": str(user_data.n_user_email),
        }
        response = requests.post(f"{AUTH_BASE_URL}/api/users", json=payload)
        if response.status_code in (200, 201):
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Authentication service is unavailable")

@app.post("/auth/generate_otp", tags=['Authentication Service'])
async def generate_otp(user_data: GenerateOtp):
    try:
        response = requests.post(f"{AUTH_BASE_URL}/api/users/generate_otp", json={"email": user_data.email})
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Authentication service is unavailable")

@app.post("/auth/verify_otp", tags=['Authentication Service'])
async def verify_otp(user_data: VerifyOtp):
    try:
        response = requests.post(f"{AUTH_BASE_URL}/api/users/verify_otp", json={"email": user_data.email, "otp": user_data.otp})
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Authentication service is unavailable")

@app.get("/auth/verification_status", tags=['Authentication Service'])
async def verification_status(email: str):
    try:
        response = requests.get(f"{AUTH_BASE_URL}/api/users/verify_email/{email}")
        data = response.json()
        if response.status_code == 200:
            return data
        else:
            detail = data.get("detail", data)
            if isinstance(detail, dict):
                raise HTTPException(status_code=response.status_code, detail=detail)
            raise HTTPException(status_code=response.status_code, detail={"message": str(detail), "code": "STATUS_ERROR"})
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail={"message": "Authentication service is unavailable", "code": "AUTH_UNAVAILABLE"})


# bicycle microservice route http://bicycle-service:5002
@app.get('/bicycles', tags=['Bicycle Service'])
async def get_bicycles(payload: dict = _fastapi.Depends(jwt_validation)):
    try:
        response = requests.get(f"{BICYCLE_SERVICE_URL}/api/bicycles")
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Bicycle service is unavailable")

@app.post('/bicycles', tags=['Bicycle Service'])
def create_bicycle(bicycle: BicycleBase, payload: dict = _fastapi.Depends(jwt_validation)):
    try:
        if bicycle.f_last_update:
            bicycle.f_last_update = bicycle.f_last_update.isoformat()
        response = requests.post(f"{BICYCLE_SERVICE_URL}/api/bicycles", json=bicycle.dict())
        if response.status_code in (200, 201):
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Bicycle service is unavailable")




if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=5001, reload=True)