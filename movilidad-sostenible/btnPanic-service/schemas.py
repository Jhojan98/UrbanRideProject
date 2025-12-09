import datetime
from datetime import datetime
from typing import Literal, Optional
from pydantic import BaseModel


class panic_buttonBase(BaseModel):
    k_id_station: int
    f_activation_date: Optional[datetime] = None

    class Config:
        from_attributes = True

class panic_buttonCreate(panic_buttonBase):
    pass
    
class panic_buttonUpdate(panic_buttonBase):
    k_id_panic_button: int
    f_activation_date: Optional[datetime] = None

class panic_buttonOut(panic_buttonBase):
    k_id_panic_button: int
    f_activation_date: datetime
    class Config:
        from_attributes = True
