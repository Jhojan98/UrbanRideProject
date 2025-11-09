from fastapi import FastAPI, HTTPException, File, UploadFile, Request
from fastapi.middleware.cors import CORSMiddleware
import fastapi as _fastapi
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from dotenv import load_dotenv
from jwt.exceptions import DecodeError, ExpiredSignatureError
import requests
import base64
import pika
import logging
import os
import jwt
import rpc_client

# Import Pydantic schemas
from schemas import UserCredentials, UserRegisteration, GenerateOtp, VerifyOtp, Bicycle
from fastapi.responses import JSONResponse

app = FastAPI()

# CORS configuration: allow Vue dev server origin
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080"],  # add other origins if needed
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.middleware("http")
async def log_raw_request(request: Request, call_next):
    try:
        body = await request.body()
        logging.info(f"[raw-request] {request.method} {request.url} content-type={request.headers.get('content-type')} body={body!r}")
    except Exception as e:
        logging.error(f"[raw-request] failed to read body: {e}")

    # Recreate request for downstream
    async def receive():
        return {"type": "http.request", "body": body}

    request = Request(request.scope, receive)
    response = await call_next(request)
    return response
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
        payload = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        return payload
    except ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token expired")
    except DecodeError:
        raise HTTPException(status_code=401, detail="Invalid JWT token")


# Authentication routes
@app.post("/auth/login", tags=['Authentication Service'])
async def login(user_data: UserCredentials):
    try:
        response = requests.post(
            f"{AUTH_BASE_URL}/api/token",
            json={"username": user_data.username, "password": user_data.password},
        )
        if response.status_code == 200:
            # El servicio de auth devuelve un JWT como string plano.
            # Normalizamos a un objeto JSON consistente {"token": "<jwt>"}.
            try:
                data = response.json()
            except Exception:
                data = response.text

            token = data if isinstance(data, str) else data.get("token")
            return {"token": token}
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Authentication service is unavailable")

@app.post("/auth/register", tags=['Authentication Service'])
async def registeration(user_data:UserRegisteration):
    try:
        response = requests.post(
            f"{AUTH_BASE_URL}/api/users",
            json={
                "name": user_data.name,
                "email": user_data.email,
                "password": user_data.password,
                "role": user_data.role or "user",
            },
        )
        if response.status_code in (200, 201):
            # Propagamos el status code real del servicio de auth
            try:
                payload = response.json()
            except Exception:
                payload = None
            return JSONResponse(status_code=response.status_code, content=payload)
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Authentication service is unavailable")

@app.post("/auth/generate_otp", tags=['Authentication Service'])
async def generate_otp(user_data:GenerateOtp):
    try:
        response = requests.post(f"{AUTH_BASE_URL}/api/users/generate_otp", json={"email":user_data.email})
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Authentication service is unavailable")

@app.post("/auth/verify_otp", tags=['Authentication Service'])
async def verify_otp(user_data:VerifyOtp):
    try:
        response = requests.post(f"{AUTH_BASE_URL}/api/users/verify_otp", json={"email":user_data.email ,"otp":user_data.otp})
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Authentication service is unavailable")


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
def create_bicycle(bicycle: Bicycle,payload: dict = _fastapi.Depends(jwt_validation)):
    try:
        response = requests.post(f"{BICYCLE_SERVICE_URL}/api/bicycles", json=bicycle.dict())
        if response.status_code == 201:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Bicycle service is unavailable")




if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=5001, reload=True)