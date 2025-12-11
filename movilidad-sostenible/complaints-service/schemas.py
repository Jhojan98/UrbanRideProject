import datetime
from datetime import datetime
from typing import Literal, Optional
from pydantic import BaseModel

class ComplaintBase(BaseModel):
    d_description: str
    t_status: Literal['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'PENDING'] = 'OPEN'
    k_id_travel: Optional[int] = None
    # Debe coincidir con el constraint de BD CHK_t_type
    t_type: Literal['BICYCLE', 'SLOT', 'STATION'] = 'BICYCLE'

class ComplaintCreate(ComplaintBase):
    pass

class ComplaintUpdate(BaseModel):
    d_description: Optional[str] = None
    t_status: Optional[Literal['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'PENDING']] = None
    k_id_travel: Optional[int] = None
    t_type: Optional[Literal['BICYCLE', 'SLOT', 'STATION']] = None

class ComplaintOut(ComplaintBase):
    k_id_complaints_and_claims: int

    class Config:
        orm_mode = True
        from_attributes = True
