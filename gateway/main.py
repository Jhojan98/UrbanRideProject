from fastapi import FastAPI, HTTPException, File, UploadFile, Request
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
from fastapi.middleware.cors import CORSMiddleware

# Import Pydantic schemas
from schemas import (
    UserCredentials,
    UserRegisteration,
    GenerateOtp,
    VerifyOtp,
    BicycleBase,
    BicycleOut,
    TravelCreateGateway,
    TravelOutGateway,
    PaymentMethodCreate,
    PaymentMethodUpdate,
    RecargaSaldoGatewayRequest,
    StripePaymentIntentGatewayRequest,
    StripeSetupIntentGatewayResponse,
)

app = FastAPI()
security = HTTPBearer()

# Load environment variables
load_dotenv()
logging.basicConfig(level=logging.INFO)

# Retrieve environment variables
JWT_SECRET = os.environ.get("JWT_SECRET")
AUTH_BASE_URL = os.environ.get("AUTH_BASE_URL")
USERS_SERVICE_URL = os.environ.get("USERS_SERVICE_URL")
RABBITMQ_URL = os.environ.get("RABBITMQ_URL", "rabbitmq")
RABBITMQ_USER = os.environ.get("RABBITMQ_DEFAULT_USER", "guest")
RABBITMQ_PASS = os.environ.get("RABBITMQ_DEFAULT_PASS", "guest")
BICYCLE_SERVICE_URL = os.environ.get("BICYCLE_SERVICE_URL")
PAYMENT_SERVICE_URL = os.environ.get("PAYMENT_SERVICE_URL")
RESERVATION_SERVICE_URL = os.environ.get("RESERVATION_SERVICE_URL")

# Helper para aplanar errores y evitar doble 'detail'
def flatten_detail(data):
    if isinstance(data, dict) and 'detail' in data and isinstance(data['detail'], dict):
        inner = data['detail']
        if isinstance(inner, dict) and 'detail' in inner and isinstance(inner['detail'], dict):
            inner = inner['detail']
        return inner
    return data

# CORS configuration
CORS_ALLOW_ORIGINS = os.environ.get("CORS_ALLOW_ORIGINS", "*")
if CORS_ALLOW_ORIGINS.strip() == "*":
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=False,
        allow_methods=["*"],
        allow_headers=["*"],
        expose_headers=["*"],
    )
else:
    origins = [o.strip() for o in CORS_ALLOW_ORIGINS.split(",") if o.strip()]
    app.add_middleware(
        CORSMiddleware,
        allow_origins=origins,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
        expose_headers=["*"],
    )

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
            return {
                "access_token": data.get("access_token"),
                "token_type": data.get("token_type", "bearer"),
                "is_verified": data.get("is_verified", False),
                "expires_in": data.get("expires_in")
            }
        else:
            detail = data.get("detail", data)
            detail = flatten_detail(detail)
            raise HTTPException(status_code=response.status_code, detail=detail)
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail={"message": "Authentication service is unavailable", "code": "AUTH_UNAVAILABLE"})

@app.post("/auth/register", tags=['Authentication Service'])
async def registeration(user_data: UserRegisteration):
    try:
        bd = user_data.f_user_birthdate
        bd_str = bd.isoformat() if hasattr(bd, "isoformat") else str(bd)
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
        if not USERS_SERVICE_URL:
            raise HTTPException(status_code=500, detail={"message": "USERS_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
        response = requests.post(f"{USERS_SERVICE_URL}/api/users", json=payload)
        data = response.json()
        if response.status_code in (200, 201):
            return data
        else:
            raise HTTPException(status_code=response.status_code, detail=flatten_detail(data))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Users service is unavailable")

@app.post("/auth/generate_otp", tags=['Authentication Service'])
async def generate_otp(user_data: GenerateOtp):
    try:
        response = requests.post(f"{AUTH_BASE_URL}/api/users/generate_otp", json={"email": user_data.email})
        data = response.json()
        if response.status_code == 200:
            return data
        else:
            raise HTTPException(status_code=response.status_code, detail=flatten_detail(data))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Authentication service is unavailable")

@app.post("/auth/verify_otp", tags=['Authentication Service'])
async def verify_otp(user_data: VerifyOtp):
    try:
        response = requests.post(f"{AUTH_BASE_URL}/api/users/verify_otp", json={"email": user_data.email, "otp": user_data.otp})
        data = response.json()
        if response.status_code == 200:
            return data
        else:
            raise HTTPException(status_code=response.status_code, detail=flatten_detail(data))
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
            raise HTTPException(status_code=response.status_code, detail=flatten_detail(detail))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail={"message": "Authentication service is unavailable", "code": "AUTH_UNAVAILABLE"})

# bicycle microservice route http://bicycle-service:5002
@app.get('/bicycles', tags=['Bicycle Service'])
async def get_bicycles(payload: dict = _fastapi.Depends(jwt_validation)):
    try:
        response = requests.get(f"{BICYCLE_SERVICE_URL}/api/bicycles")
        data = response.json()
        if response.status_code == 200:
            return data
        else:
            raise HTTPException(status_code=response.status_code, detail=flatten_detail(data))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Bicycle service is unavailable")

@app.post('/bicycles', tags=['Bicycle Service'])
def create_bicycle(bicycle: BicycleBase, payload: dict = _fastapi.Depends(jwt_validation)):
    try:
        if bicycle.f_last_update:
            bicycle.f_last_update = bicycle.f_last_update.isoformat()
        response = requests.post(f"{BICYCLE_SERVICE_URL}/api/bicycles", json=bicycle.dict())
        data = response.json()
        if response.status_code in (200, 201):
            return data
        else:
            raise HTTPException(status_code=response.status_code, detail=flatten_detail(data))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Bicycle service is unavailable")

@app.post("/reservations", response_model=TravelOutGateway, tags=['Reservation Service'])
async def create_reservation(data: TravelCreateGateway, payload: dict = _fastapi.Depends(jwt_validation)):
    if not RESERVATION_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "RESERVATION_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
    k_user_cc = payload.get("k_user_cc")
    if not k_user_cc:
        raise HTTPException(status_code=401, detail="User ID missing in token")
    body = {
        "k_user_cc": k_user_cc,
        "k_series": data.k_series,
        "k_id_bicycle": data.k_id_bicycle,
        "k_metodo_pago": data.k_metodo_pago,
    }
    try:
        response = requests.post(f"{RESERVATION_SERVICE_URL}/api/reservations/", json=body)
        data_resp = response.json()
        if response.status_code in (200, 201):
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Reservation service is unavailable")

@app.get("/reservations", response_model=list[TravelOutGateway], tags=['Reservation Service'])
async def list_user_reservations(payload: dict = _fastapi.Depends(jwt_validation)):
    if not RESERVATION_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "RESERVATION_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
    k_user_cc = payload.get("k_user_cc")
    if not k_user_cc:
        raise HTTPException(status_code=401, detail="User ID missing in token")
    try:
        response = requests.get(f"{RESERVATION_SERVICE_URL}/api/reservations/user/{k_user_cc}")
        data_resp = response.json()
        if response.status_code == 200:
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Reservation service is unavailable")

@app.get("/reservations/{travel_id}", response_model=TravelOutGateway, tags=['Reservation Service'])
async def get_reservation(travel_id: int, payload: dict = _fastapi.Depends(jwt_validation)):
    if not RESERVATION_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "RESERVATION_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
    k_user_cc = payload.get("k_user_cc")
    if not k_user_cc:
        raise HTTPException(status_code=401, detail="User ID missing in token")
    try:
        response = requests.get(f"{RESERVATION_SERVICE_URL}/api/reservations/{travel_id}/user/{k_user_cc}")
        data_resp = response.json()
        if response.status_code == 200:
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Reservation service is unavailable")

@app.delete("/reservations/{travel_id}", tags=['Reservation Service'])
async def cancel_reservation(travel_id: int, payload: dict = _fastapi.Depends(jwt_validation)):
    if not RESERVATION_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "RESERVATION_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
    k_user_cc = payload.get("k_user_cc")
    if not k_user_cc:
        raise HTTPException(status_code=401, detail="User ID missing in token")
    try:
        response = requests.delete(f"{RESERVATION_SERVICE_URL}/api/reservations/{travel_id}/user/{k_user_cc}")
        data_resp = response.json()
        if response.status_code == 200:
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Reservation service is unavailable")

@app.post("/payments/methods", tags=["Payment Service"])
async def create_payment_method(data: PaymentMethodCreate, payload: dict = _fastapi.Depends(jwt_validation)):
    if not PAYMENT_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "PAYMENT_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
    k_user_cc = payload.get("k_user_cc")
    if not k_user_cc:
        raise HTTPException(status_code=401, detail="User ID missing in token")
    body = data.dict()
    # Asegurarnos de que las fechas sean serializables a JSON
    if body.get("f_fecha_expiracion") is not None:
        fe = body["f_fecha_expiracion"]
        if hasattr(fe, "isoformat"):
            body["f_fecha_expiracion"] = fe.isoformat()
    body["k_usuario_cc"] = k_user_cc

    try:
        response = requests.post(f"{PAYMENT_SERVICE_URL}/api/metodos-pago/", json=body)
        data_resp = response.json()
        if response.status_code in (200, 201):
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")

@app.get("/payments/methods", tags=["Payment Service"])
async def list_payment_methods(payload: dict = _fastapi.Depends(jwt_validation)):
    if not PAYMENT_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "PAYMENT_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
    k_user_cc = payload.get("k_user_cc")
    if not k_user_cc:
        raise HTTPException(status_code=401, detail="User ID missing in token")
    try:
        response = requests.get(f"{PAYMENT_SERVICE_URL}/api/metodos-pago/usuario/{k_user_cc}")
        data_resp = response.json()
        if response.status_code == 200:
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")

@app.get("/payments/methods/principal", tags=["Payment Service"])
async def get_principal_method(payload: dict = _fastapi.Depends(jwt_validation)):
    if not PAYMENT_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "PAYMENT_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
    k_user_cc = payload.get("k_user_cc")
    if not k_user_cc:
        raise HTTPException(status_code=401, detail="User ID missing in token")
    try:
        response = requests.get(f"{PAYMENT_SERVICE_URL}/api/metodos-pago/usuario/{k_user_cc}/principal")
        data_resp = response.json()
        if response.status_code == 200:
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")

@app.put("/payments/methods/{method_id}", tags=["Payment Service"])
async def update_payment_method(method_id: int, data: PaymentMethodUpdate, payload: dict = _fastapi.Depends(jwt_validation)):
    if not PAYMENT_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "PAYMENT_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
    k_user_cc = payload.get("k_user_cc")
    if not k_user_cc:
        raise HTTPException(status_code=401, detail="User ID missing in token")
    body = {k: v for k, v in data.dict().items() if v is not None}
    # Normalizar fecha a string ISO si viene en el body
    if body.get("f_fecha_expiracion") is not None:
        fe = body["f_fecha_expiracion"]
        if hasattr(fe, "isoformat"):
            body["f_fecha_expiracion"] = fe.isoformat()

    try:
        response = requests.put(f"{PAYMENT_SERVICE_URL}/api/metodos-pago/{method_id}/usuario/{k_user_cc}", json=body)
        data_resp = response.json()
        if response.status_code == 200:
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")

@app.post("/payments/recharge", tags=["Payment Service"])
async def recharge_balance(data: RecargaSaldoGatewayRequest, payload: dict = _fastapi.Depends(jwt_validation)):
    if not PAYMENT_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "PAYMENT_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
    try:
        response = requests.post(f"{PAYMENT_SERVICE_URL}/api/metodos-pago/recarga", json=data.dict())
        data_resp = response.json()
        if response.status_code == 200:
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")

@app.get("/payments/balance", tags=["Payment Service"])
async def get_total_balance(payload: dict = _fastapi.Depends(jwt_validation)):
    if not PAYMENT_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "PAYMENT_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
    k_user_cc = payload.get("k_user_cc")
    if not k_user_cc:
        raise HTTPException(status_code=401, detail="User ID missing in token")
    try:
        response = requests.get(f"{PAYMENT_SERVICE_URL}/api/metodos-pago/usuario/{k_user_cc}/saldo-total")
        data_resp = response.json()
        if response.status_code == 200:
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")

@app.post("/payments/stripe/setup-intent", response_model=StripeSetupIntentGatewayResponse, tags=["Payment Service"])
async def create_stripe_setup_intent(payload: dict = _fastapi.Depends(jwt_validation)):
    if not PAYMENT_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "PAYMENT_SERVICE_URL not configured", "code": "CONFIG_ERROR"})

    k_user_cc = payload.get("k_user_cc")
    if not k_user_cc:
        raise HTTPException(status_code=401, detail="User ID missing in token")

    body = {"k_usuario_cc": k_user_cc}

    try:
        response = requests.post(f"{PAYMENT_SERVICE_URL}/api/metodos-pago/stripe/setup-intent", json=body)
        data_resp = response.json()
        if response.status_code == 200:
            # Incluir también el k_usuario_cc asociado al token en la respuesta al frontend
            data_resp["k_usuario_cc"] = k_user_cc
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")

@app.post("/payments/stripe/payment-intent", tags=["Payment Service"])
async def create_stripe_payment_intent(data: StripePaymentIntentGatewayRequest, payload: dict = _fastapi.Depends(jwt_validation)):
    if not PAYMENT_SERVICE_URL:
        raise HTTPException(status_code=500, detail={"message": "PAYMENT_SERVICE_URL not configured", "code": "CONFIG_ERROR"})
    try:
        response = requests.post(f"{PAYMENT_SERVICE_URL}/api/metodos-pago/stripe/payment-intent", json=data.dict())
        data_resp = response.json()
        if response.status_code == 200:
            return data_resp
        raise HTTPException(status_code=response.status_code, detail=flatten_detail(data_resp))
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Payment service is unavailable")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=5001, reload=True)