from typing import Optional
"""
Pydantic schemas for request/response validation
"""
from pydantic import BaseModel, EmailStr


class GenerateUserToken(BaseModel):
    username: str
    password: str


class UserCredentials(BaseModel):
    username: str
    password: str


class UserRegisteration(BaseModel):
    name: str
    email: str
    password: str
    role: Optional[str] = "user"


class GenerateOtp(BaseModel):
    email: str


class VerifyOtp(BaseModel):
    email: str
    otp: int

class Bicycle(BaseModel):
    serie: str
    modelo: str
    estado: str


# Payment Method Schemas
class PaymentMethodCreate(BaseModel):
    k_usuario_cc: int  # Changed from str to int to match database
    t_tipo_tarjeta: str  # CREDITO, DEBITO, PSE, EFECTIVO
    n_nombre_titular: str
    f_fecha_expiracion: str  # formato: YYYY-MM-DD
    n_numero_tarjeta_completo: Optional[str] = None
    n_direccion_facturacion: Optional[str] = None
    n_codigo_postal: Optional[str] = None
    b_principal: Optional[bool] = False

class RechargeBalance(BaseModel):
    k_metodo_pago: int
    monto: int
    descripcion: Optional[str] = None

# Reservation Schemas
class ReservationCreate(BaseModel):
    k_user_cc: int
    k_series: int
    k_id_bicycle: int
    k_metodo_pago: int
