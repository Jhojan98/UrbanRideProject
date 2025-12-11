from datetime import date
from typing import Optional, Literal

from pydantic import BaseModel, Field


ALLOWED_MAINTENANCE_TYPES = ("PREVENTIVE", "CORRECTIVE", "INSPECTION")
ALLOWED_ENTITY_TYPES = ("BICYCLE", "STATION", "LOCK")
ALLOWED_TRIGGERED_BY = ("ADMIN", "USER", "IOT_ALERT")


class MaintenanceRecordBase(BaseModel):
    t_entity_type: Literal["BICYCLE", "STATION", "LOCK"] = Field(
        alias="entityType",
    )
    t_maintenance_type: Literal["PREVENTIVE", "CORRECTIVE", "INSPECTION"] = Field(
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
    k_id_maintenance: str = Field(alias="id")


class MaintenancePartialUpdate(BaseModel):
    t_entity_type: Optional[Literal["BICYCLE", "STATION", "LOCK"]] = Field(default=None, alias="entityType")
    t_maintenance_type: Optional[Literal["PREVENTIVE", "CORRECTIVE", "INSPECTION"]] = Field(
        default=None,
        alias="maintenanceType",
    )
    t_triggered_by: Optional[Literal["ADMIN", "USER", "IOT_ALERT"]] = Field(default=None, alias="triggeredBy")
    d_description: Optional[str] = Field(default=None, alias="description")
    t_status: Optional[Literal['PENDING', 'SOLVING', 'RESOLVED']] = Field(default=None, alias="status")
    f_date: Optional[date] = Field(default=None, alias="date")
    v_cost: Optional[int] = Field(default=None, alias="cost")
    k_id_bicycle: Optional[str] = Field(default=None, alias="bikeId")
    k_id_station: Optional[int] = Field(default=None, alias="stationId")
    k_id_slot: Optional[str] = Field(default=None, alias="lockId")

    class Config:
        populate_by_name = True
        from_attributes = True

class MaintenanceStatusUpdate(BaseModel):
    t_status: Literal['PENDING', 'SOLVING', 'RESOLVED'] = Field(alias="status")

    class Config:
        populate_by_name = True
