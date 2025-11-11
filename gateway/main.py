from fastapi import FastAPI, HTTPException, File, UploadFile
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
from schemas import (
    UserCredentials, 
    UserRegisteration, 
    GenerateOtp, 
    VerifyOtp, 
    Bicycle, 
    PaymentMethodCreate,
    RechargeBalance,
    ReservationCreate
)

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
PAYMENT_SERVICE_URL = os.getenv('PAYMENT_SERVICE_URL', 'http://payment-service:5003')
RESERVATION_SERVICE_URL = os.getenv('RESERVATION_SERVICE_URL', 'http://reservation-service:5004')

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
        response = requests.post(f"{AUTH_BASE_URL}/api/token", json={"username": user_data.username, "password": user_data.password})
        if response.status_code == 200:
            return response.json()
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
        if response.status_code == 200:
            return response.json()
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


# Payment Methods microservice routes
@app.post('/payment-methods', tags=['Payment Methods Service'])
async def create_payment_method(payment_data: PaymentMethodCreate, payload: dict = _fastapi.Depends(jwt_validation)):
    """
    Crear un nuevo método de pago para un usuario.
    Requiere autenticación JWT.
    """
    try:
        response = requests.post(
            f"{PAYMENT_SERVICE_URL}/api/metodos-pago/",
            json=payment_data.dict()
        )
        if response.status_code == 201:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")


@app.get('/payment-methods/user/{user_cc}', tags=['Payment Methods Service'])
async def get_payment_methods(user_cc: int, payload: dict = _fastapi.Depends(jwt_validation)):
    """
    Obtener todos los métodos de pago activos de un usuario.
    Requiere autenticación JWT.
    """
    try:
        response = requests.get(f"{PAYMENT_SERVICE_URL}/api/metodos-pago/usuario/{user_cc}")
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")


@app.delete('/payment-methods/{payment_id}/user/{user_cc}', tags=['Payment Methods Service'])
async def delete_payment_method(payment_id: int, user_cc: int, payload: dict = _fastapi.Depends(jwt_validation)):
    """
    Eliminar (desactivar) un método de pago específico.
    Requiere autenticación JWT.
    """
    try:
        response = requests.delete(
            f"{PAYMENT_SERVICE_URL}/api/metodos-pago/{payment_id}/usuario/{user_cc}"
        )
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")


@app.post('/payment-methods/recharge', tags=['Payment Methods Service'])
async def recharge_balance(recharge_data: RechargeBalance, payload: dict = _fastapi.Depends(jwt_validation)):
    """
    Recargar saldo en un método de pago.
    Requiere autenticación JWT.
    """
    try:
        response = requests.post(
            f"{PAYMENT_SERVICE_URL}/api/metodos-pago/recarga",
            json=recharge_data.dict()
        )
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")


@app.get('/payment-methods/balance/{payment_method_id}', tags=['Payment Methods Service'])
async def get_balance(payment_method_id: int, payload: dict = _fastapi.Depends(jwt_validation)):
    """
    Consultar saldo de un método de pago.
    Requiere autenticación JWT.
    """
    try:
        response = requests.get(f"{PAYMENT_SERVICE_URL}/api/metodos-pago/saldo/{payment_method_id}")
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")


@app.get('/payment-methods/balance/user/{user_cc}/total', tags=['Payment Methods Service'])
async def get_user_total_balance(user_cc: int, payload: dict = _fastapi.Depends(jwt_validation)):
    """
    Consultar saldo total de todos los métodos de pago del usuario.
    Requiere autenticación JWT.
    """
    try:
        response = requests.get(f"{PAYMENT_SERVICE_URL}/api/metodos-pago/usuario/{user_cc}/saldo-total")
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")


# Reservation microservice routes
@app.post('/reservations', tags=['Reservation Service'])
async def create_reservation(reservation_data: ReservationCreate, payload: dict = _fastapi.Depends(jwt_validation)):
    """
    Crear una nueva reserva/viaje con cobro automático.
    Se descuentan $5,000 del método de pago.
    Requiere autenticación JWT.
    """
    try:
        response = requests.post(
            f"{RESERVATION_SERVICE_URL}/api/reservations/",
            json=reservation_data.dict()
        )
        if response.status_code == 201:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Reservation service is unavailable")


@app.get('/reservations/user/{user_cc}', tags=['Reservation Service'])
async def get_user_reservations(user_cc: int, payload: dict = _fastapi.Depends(jwt_validation)):
    """
    Obtener todas las reservas de un usuario.
    Requiere autenticación JWT.
    """
    try:
        response = requests.get(f"{RESERVATION_SERVICE_URL}/api/reservations/user/{user_cc}")
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Reservation service is unavailable")


@app.get('/reservations/{reservation_id}', tags=['Reservation Service'])
async def get_reservation(reservation_id: int, payload: dict = _fastapi.Depends(jwt_validation)):
    """
    Obtener una reserva específica.
    Requiere autenticación JWT.
    """
    try:
        response = requests.get(f"{RESERVATION_SERVICE_URL}/api/reservations/{reservation_id}")
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Reservation service is unavailable")


@app.delete('/reservations/{reservation_id}/user/{user_cc}', tags=['Reservation Service'])
async def cancel_reservation(reservation_id: int, user_cc: int, payload: dict = _fastapi.Depends(jwt_validation)):
    """
    Cancelar una reserva.
    Requiere autenticación JWT.
    """
    try:
        response = requests.delete(
            f"{RESERVATION_SERVICE_URL}/api/reservations/{reservation_id}/user/{user_cc}"
        )
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(status_code=response.status_code, detail=response.json())
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Reservation service is unavailable")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=5001, reload=True)