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
    f_user_registration_date: _dt.date
    t_is_verified: bool

    class Config:
        from_attributes = True

# Representación pública
class User(UserBase):
    pass

class UserCreate(BaseModel):
    k_user_cc: int = Field(...)
    n_username: str = Field(..., min_length=3, max_length=50)
    password: str = Field(..., min_length=6)
    n_user_first_name: str = Field(..., max_length=50)
    n_user_second_name: Optional[str] = Field(default=None, max_length=50)
    n_user_first_lastname: str = Field(..., max_length=50)
    n_user_second_lastname: Optional[str] = Field(default=None, max_length=50)
    f_user_birthdate: _dt.date
    n_user_email: EmailStr

class RegisterResponse(BaseModel):
    detail: str
    k_user_cc: int

class GenerateUserToken(BaseModel):
    username: str  # n_user_email or n_username
    password: str

class GenerateOtp(BaseModel):
    email: EmailStr
class VerifyOtp(BaseModel):
    email: EmailStr
    otp: str
