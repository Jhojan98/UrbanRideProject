from typing import Optional
"""
Pydantic schemas for request/response validation
"""
from pydantic import BaseModel, Field, EmailStr
import datetime as _dt


class UserRegisteration(BaseModel):
    k_user_cc: int = Field(..., description="User ID", example=123456789)
    n_username: str = Field(..., min_length=3, max_length=50, example="jhojan_ara")
    password: str = Field(..., example="123456")
    n_user_first_name: str = Field(..., max_length=100, example="Jhojan")
    n_user_second_name: Optional[str] = Field(default="", example="Miguel")
    n_user_first_lastname: str = Field(..., max_length=100, example="Arango")
    n_user_second_lastname: Optional[str] = Field(default="", example="")
    f_user_birthdate: _dt.date = Field(..., example="2000-12-02")
    n_user_email: EmailStr = Field(..., example="jhojan@example.com")


class GenerateUserToken(BaseModel):
    username: str
    password: str


class UserCredentials(BaseModel):
    username: str
    password: str


# Bicycle schemas aligned to English table 'bicycle'
class BicycleBase(BaseModel):
    k_series: int
    n_model: str
    t_padlock_status: str
    f_last_update: Optional[_dt.datetime] = None
    n_latitude: Optional[float] = None
    n_length: Optional[float] = None
    v_battery: Optional[float] = None


class BicycleCreate(BicycleBase):
    pass


class BicycleUpdate(BaseModel):
    n_model: Optional[str] = None
    t_padlock_status: Optional[str] = None
    f_last_update: Optional[_dt.datetime] = None
    n_latitude: Optional[float] = None
    n_length: Optional[float] = None
    v_battery: Optional[float] = None


class BicycleOut(BicycleBase):
    k_id_bicycle: int

    class Config:
        orm_mode = True
        from_attributes = True


class GenerateOtp(BaseModel):
    email: EmailStr


class VerifyOtp(BaseModel):
    email: EmailStr
    otp: str  # Cambiado a string


class TravelCreateGateway(BaseModel):
    k_series: int
    k_id_bicycle: int
    k_metodo_pago: int


class TravelOutGateway(BaseModel):
    k_id_travel: int
    f_request_date: Optional[_dt.datetime]
    k_user_cc: int
    k_series: int
    k_id_bicycle: int

    class Config:
        from_attributes = True


class PaymentMethodCreate(BaseModel):
    t_tipo_tarjeta: str
    n_nombre_titular: str
    f_fecha_expiracion: _dt.date
    n_direccion_facturacion: Optional[str] = None
    n_codigo_postal: Optional[str] = None
    b_principal: Optional[bool] = False
    n_numero_tarjeta_completo: Optional[str] = None


class PaymentMethodUpdate(BaseModel):
    n_nombre_titular: Optional[str] = None
    f_fecha_expiracion: Optional[_dt.date] = None
    n_direccion_facturacion: Optional[str] = None
    n_codigo_postal: Optional[str] = None


class RecargaSaldoGatewayRequest(BaseModel):
    k_metodo_pago: int
    monto: int
    descripcion: Optional[str] = None


class StripePaymentIntentGatewayRequest(BaseModel):
    k_metodo_pago: int
    monto: int
    moneda: Optional[str] = None


class StripeSetupIntentGatewayResponse(BaseModel):
    setup_intent_id: str
    client_secret: str
    customer_id: str
    k_usuario_cc: int