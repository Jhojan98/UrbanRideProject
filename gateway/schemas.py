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
