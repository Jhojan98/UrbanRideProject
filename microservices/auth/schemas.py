import datetime as _dt
from pydantic import BaseModel, EmailStr, Field
from typing import Optional

# Esquema base alineado con la tabla usuario
class UserBase(BaseModel):
    k_user_cc: int
    n_username: str
    n_user_first_name: str
    n_user_second_name: Optional[str] = None
    n_user_first_lastname: str
    n_user_second_lastname: Optional[str] = None
    f_user_birthdate: _dt.date
    n_user_email: EmailStr
    t_subscription_type: str
    f_user_registration_date: str
    t_is_verified: bool

    class Config:
        from_attributes = True

# Datos para crear usuario (solo campos mínimos + password).
class UserCreate(BaseModel):
    k_user_cc: int = Field(..., description="User ID", example=123456789)
    n_username: str = Field(..., min_length=3, max_length=50, example="jhojan_ara")
    password: str = Field(..., min_length=8, example="123456")
    n_user_first_name: str = Field(..., max_length=100, example="Jhojan")
    n_user_second_name: Optional[str] = Field(default="", example="Miguel")
    n_user_first_lastname: str = Field(..., max_length=100, example="Arango")
    n_user_second_lastname: Optional[str] = Field(default="", example="")
    f_user_birthdate: _dt.date = Field(..., example="2000-12-02")
    n_user_email: EmailStr = Field(..., example="jhojan@example.com")

# Representación pública
class User(UserBase):
    pass

class GenerateUserToken(BaseModel):
    username: str  # n_user_email or n_username
    password: str

class GenerateOtp(BaseModel):
    email: EmailStr
class VerifyOtp(BaseModel):
    email: EmailStr
    otp: str
