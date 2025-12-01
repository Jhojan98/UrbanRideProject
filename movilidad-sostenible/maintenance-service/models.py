from sqlalchemy import Column, Date, ForeignKey, Integer, Numeric, String
from database import Base


class MaintenanceRecord(Base):
    __tablename__ = "maintenance"

    k_id_maintenance = Column(Integer, primary_key=True, index=True)
    t_maintenance_type = Column(String(50), nullable=False)
    v_total_trips = Column(Integer, nullable=False)
    t_triggered_by = Column(String(50), nullable=False)
    d_description = Column(String(250), nullable=False)
    t_status = Column(String(50), nullable=False)
    f_date = Column(Date, nullable=False)
    k_id_bicycle = Column(String(11), ForeignKey("bicycle.k_id_bicycle"), nullable=True)
    v_cost_usd = Column(Numeric(10, 2), nullable=True)
