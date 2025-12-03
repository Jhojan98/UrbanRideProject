import datetime
from datetime import datetime
from typing import Literal, Optional
from pydantic import BaseModel


class FineBase(BaseModel):
    d_description: str
    v_amount: int


class FineCreate(FineBase):
    pass


class FineUpdate(BaseModel):
    d_description: Optional[str] = None
    v_amount: Optional[int] = None


class FineOut(FineBase):
    k_id_fine: int

    class Config:
        orm_mode = True
        from_attributes = True


class UserFineBase(BaseModel):
    n_reason: str
    t_state: Literal['PENDING', 'PAID'] = 'PENDING'
    k_id_fine: int
    k_uid_user: Optional[str] = None


class UserFineCreate(UserFineBase):
    pass


class UserFineUpdate(BaseModel):
    n_reason: Optional[str] = None
    t_state: Optional[Literal['PENDING', 'PAID']] = None
    k_id_fine: Optional[int] = None
    k_uid_user: Optional[str] = None


class UserFineOut(UserFineBase):
    k_user_fine: int
    v_amount_snapshot: Optional[int] = None
    f_assigned_at: datetime
    f_update_at: Optional[datetime] = None
    fine: Optional[FineOut] = None

    class Config:
        orm_mode = True
        from_attributes = True
