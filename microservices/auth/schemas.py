import datetime as _dt
from pydantic import BaseModel
from typing import Optional

class _AddressBase(BaseModel):
    street: str
    landmark: str
    city: str
    country: str
    pincode: str
    latitude: float
    longitude: float

class AddressCreate(_AddressBase):
    pass

class Address(_AddressBase):
    id: int
    user_id: int

    class Config:
        from_attributes = True

class _UserBase(BaseModel):
    name: str
    email: str

class UserCreate(_UserBase):
    password: str
    role: Optional[str] = "user"

class User(_UserBase):
    id: int
    is_verified: bool
    date_created: _dt.datetime
    role: str = "user"
    addresses: list[Address] = []

    class Config:
        from_attributes = True

class GenerateUserToken(BaseModel):
    username: str
    password: str

class GenerateOtp(BaseModel):
    email: str

class VerifyOtp(BaseModel):
    email: str
    otp: int
