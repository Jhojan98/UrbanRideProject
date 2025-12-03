from datetime import date
from typing import Optional, Literal

from pydantic import BaseModel, Field

ALLOWED_MAINTENANCE_TYPES = ("PREVENTIVO", "CORRECTIVO", "EMERGENCIA")


class MaintenanceRecordBase(BaseModel):
    t_maintenance_type: Literal["PREVENTIVO", "CORRECTIVO", "EMERGENCIA"] = Field(
        default="PREVENTIVO",
        alias="maintenanceType",
    )
    v_total_trips: int = Field(alias="totalTrips")
    t_triggered_by: str = Field(alias="triggeredBy")
    d_description: str = Field(alias="description")
    t_status: Literal['PENDING', 'SOLVING', 'RESOLVED'] = Field(
        default='PENDING',
        alias="status",
    )
    f_date: date = Field(alias="date")
    k_id_bicycle: Optional[str] = Field(default=None, alias="bikeId")
    v_cost_usd: Optional[float] = Field(default=None, alias="costUSD")

    class Config:
        populate_by_name = True
        from_attributes = True


class MaintenanceRecordCreate(MaintenanceRecordBase):
    pass


class MaintenanceRecord(MaintenanceRecordBase):
    k_id_maintenance: int = Field(alias="id")
