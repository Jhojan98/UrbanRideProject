from datetime import date
from typing import Optional, Literal

from pydantic import BaseModel, Field


ALLOWED_MAINTENANCE_TYPES = ("PREVENTIVE", "CORRECTIVE", "INSPECTION")
ALLOWED_ENTITY_TYPES = ('BiCYCLE', 'STATION', 'SLOT')
ALLOWED_TRIGGERED_BY = ("ADMIN", "USER", "IOT_ALERT")


class MaintenanceRecordBase(BaseModel):
    t_entity_type: Literal['BiCYCLE', 'STATION', 'SLOT'] = Field(
        alias="entityType",
    )
    t_maintance_type: Literal["PREVENTIVE", "CORRECTIVE", "INSPECTION"] = Field(
        default="PREVENTIVE",
        alias="maintenanceType",
    )
    t_triggered_by: Literal["ADMIN", "USER", "IOT_ALERT"] = Field(alias="triggeredBy")
    d_description: str = Field(alias="description")
    t_status: Literal['PENDING', 'SOLVING', 'RESOLVED'] = Field(
        default='PENDING',
        alias="status",
    )
    f_date: date = Field(alias="date")
    v_cost: Optional[int] = Field(default=None, alias="cost")
    k_id_bicycle: Optional[str] = Field(default=None, alias="bikeId")
    k_id_station: Optional[int] = Field(default=None, alias="stationId")
    k_id_slot: Optional[str] = Field(default=None, alias="lockId")

    class Config:
        populate_by_name = True
        from_attributes = True


class MaintenanceRecordCreate(MaintenanceRecordBase):
    pass


class MaintenanceRecord(MaintenanceRecordBase):
    k_id_maintenance: int = Field(alias="id")
