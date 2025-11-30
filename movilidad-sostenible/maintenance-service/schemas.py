from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional

class MaintenanceRecordBase(BaseModel):
    k_id_bicycle: int = Field(alias="bikeId")
    t_maintenance_type: str = Field(alias="maintenanceType")
    f_scheduled_date: datetime = Field(alias="scheduledDate")
    f_completed_date: Optional[datetime] = Field(default=None, alias="completedDate")
    v_total_trips: Optional[int] = Field(default=None, alias="totalTrips")
    t_triggered_by: str = Field(alias="triggeredBy")
    k_technician_id: str = Field(alias="technicianId")
    k_id_station: int = Field(alias="stationId")
    v_cost_usd: Optional[float] = Field(default=None, alias="costUSD")
    n_notes: Optional[str] = Field(default=None, alias="notes")

    class Config:
        populate_by_name = True
        from_attributes = True

class MaintenanceRecordCreate(MaintenanceRecordBase):
    pass

class MaintenanceRecord(MaintenanceRecordBase):
    k_id_maintenance: int = Field(alias="id")
