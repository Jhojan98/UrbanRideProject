import datetime as _dt
from pydantic import BaseModel, EmailStr, Field
from typing import Optional

# Esquema base alineado con la tabla usuario
class UserBase(BaseModel):
    k_cedula_ciudadania_usuario: int
    n_usuario: str
    n_primer_nombre: str
    n_segundo_nombre: Optional[str] = None
    n_primer_apellido: str
    n_segundo_apellido: Optional[str] = None
    f_fecha_nacimiento: _dt.date
    n_correo_electronico: EmailStr
    t_tipo_suscripcion: str
    f_fecha_de_registro: str
    is_verified: bool
    otp: Optional[str] = None
    date_created: _dt.datetime

    class Config:
        from_attributes = True

# Datos para crear usuario (solo campos mínimos + password).
class UserCreate(BaseModel):
    n_usuario: str
    password: str = Field(min_length=6)
    n_primer_nombre: str
    n_segundo_nombre: Optional[str] = None
    n_primer_apellido: str
    n_segundo_apellido: Optional[str] = None
    f_fecha_nacimiento: _dt.date
    n_correo_electronico: EmailStr

# Representación pública
class User(UserBase):
    pass

class GenerateUserToken(BaseModel):
    username: str  # n_correo_electronico o n_usuario según la lógica en authenticate
    password: str

class GenerateOtp(BaseModel):
    email: EmailStr

class VerifyOtp(BaseModel):
    email: EmailStr
    otp: str
