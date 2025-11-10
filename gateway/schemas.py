from typing import Optional
"""
Pydantic schemas for request/response validation
"""
from pydantic import BaseModel, EmailStr
import datetime as _dt


class GenerateUserToken(BaseModel):
    username: str
    password: str


class UserCredentials(BaseModel):
    username: str
    password: str


class UserRegisteration(BaseModel):
    n_usuario: str
    password: str
    n_primer_nombre: str
    n_segundo_nombre: Optional[str] = None
    n_primer_apellido: str
    n_segundo_apellido: Optional[str] = None
    f_fecha_nacimiento: str  # Enviar como ISO date (YYYY-MM-DD)
    n_correo_electronico: EmailStr


class GenerateOtp(BaseModel):
    email: EmailStr


class VerifyOtp(BaseModel):
    email: EmailStr
    otp: str  # Cambiado a string


class BicycleBase(BaseModel):
    k_serie: int
    n_modelo: str
    t_estado_candado: str
    f_ultima_actualizacion: Optional[_dt.datetime] = None
    n_latitud: Optional[float] = None
    n_longitud: Optional[float] = None
    v_bateria: Optional[float] = None


class BicycleCreate(BicycleBase):
    pass


class BicycleUpdate(BaseModel):
    n_modelo: Optional[str] = None
    t_estado_candado: Optional[str] = None
    f_ultima_actualizacion: Optional[_dt.datetime] = None
    n_latitud: Optional[float] = None
    n_longitud: Optional[float] = None
    v_bateria: Optional[float] = None


class BicycleOut(BicycleBase):
    k_id_bicicleta: int

    class Config:
        orm_mode = True
        from_attributes = True