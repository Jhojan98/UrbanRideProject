from sqlalchemy import Column, Integer, String, DateTime, Numeric, ForeignKey
from sqlalchemy.orm import relationship
from database import Base

class MaintenanceRecord(Base):
    __tablename__ = "maintenance_record"

    k_id_maintenance = Column(Integer, primary_key=True, index=True)
    k_id_bicycle = Column(Integer, nullable=False, index=True)
    t_maintenance_type = Column(String(50), nullable=False)
    f_scheduled_date = Column(DateTime, nullable=False)
    f_completed_date = Column(DateTime, nullable=True)
    v_total_trips = Column(Integer, nullable=True)
    t_triggered_by = Column(String(50), nullable=False)
    k_technician_id = Column(String(50), nullable=False)
    k_id_station = Column(Integer, nullable=False, index=True)
    v_cost_usd = Column(Numeric(10, 2), nullable=True)
    n_notes = Column(String(500), nullable=True)
