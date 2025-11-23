from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field

class TravelBase(BaseModel):
    k_user_cc: int = Field(..., gt=0, description="Cédula del usuario")
    k_series: int = Field(..., gt=0, description="Serie de la bicicleta")
    k_id_bicycle: int = Field(..., gt=0, description="ID de la bicicleta")

class TravelCreate(TravelBase):
    k_metodo_pago: int = Field(..., gt=0, description="ID del método de pago para descontar")

class TravelOut(BaseModel):
    k_id_travel: int
    f_request_date: Optional[datetime]
    k_user_cc: int
    k_series: int
    k_id_bicycle: int

    class Config:
        from_attributes = True
