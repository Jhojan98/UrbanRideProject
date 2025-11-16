import datetime as _dt
from pydantic import BaseModel, EmailStr, Field, validator
from typing import Optional

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

class UserUpdate(BaseModel):
    n_user_first_name: Optional[str] = Field(default=None, max_length=50)
    n_user_second_name: Optional[str] = Field(default=None, max_length=50)
    n_user_first_lastname: Optional[str] = Field(default=None, max_length=50)
    n_user_second_lastname: Optional[str] = Field(default=None, max_length=50)
    f_user_birthdate: Optional[_dt.date] = None
    t_subscription_type: Optional[str] = Field(default=None, max_length=50)
    t_is_verified: Optional[bool] = None  # allow auth-service to mark verified

class UserOut(UserBase):
    pass

# New schema to allow creating a user from this service
class UserCreate(BaseModel):
    k_user_cc: int = Field(..., description="User ID (cedula)", gt=0)
    n_username: str = Field(..., min_length=3, max_length=50)
    password: str = Field(..., min_length=6, max_length=128)  # No complejidad adicional
    n_user_first_name: str = Field(..., max_length=50)
    n_user_second_name: Optional[str] = Field(default=None, max_length=50)
    n_user_first_lastname: str = Field(..., max_length=50)
    n_user_second_lastname: Optional[str] = Field(default=None, max_length=50)
    f_user_birthdate: _dt.date
    n_user_email: EmailStr

    @validator('n_username', 'n_user_first_name', 'n_user_second_name', 'n_user_first_lastname', 'n_user_second_lastname', pre=True, always=True)
    def strip_whitespace(cls, v):
        if v is None:
            return v
        return v.strip()

    @validator('f_user_birthdate')
    def birthdate_not_future(cls, v: _dt.date):
        if v > _dt.date.today():
            raise ValueError('Birthdate cannot be in the future')
        return v
