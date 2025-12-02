from sqlalchemy import Column, Date, ForeignKey, Integer, Numeric, String, CheckConstraint
from database import Base


class MaintenanceRecord(Base):
    __tablename__ = "maintenance"
    __table_args__ = (
        CheckConstraint("t_status IN ('PENDING', 'SOLVING', 'RESOLVED')", name="CHK_t_status"),
    )

    k_id_maintenance = Column(Integer, primary_key=True, index=True)
    t_maintenance_type = Column(String(50), nullable=False)
    v_total_trips = Column(Integer, nullable=False)
    t_triggered_by = Column(String(50), nullable=False)
    d_description = Column(String(250), nullable=False)
    t_status = Column(String(50), nullable=False, server_default='PENDING')
    f_date = Column(Date, nullable=False)
    k_id_bicycle = Column(String(11), ForeignKey("public.bicycle.k_id_bicycle"), nullable=True)
    v_cost_usd = Column(Numeric(10, 2), nullable=True)

class __BicycleReference(Base):
    """Register `public.bicycle` so ForeignKey resolution works when creating metadata."""
    __tablename__ = "bicycle"
    __table_args__ = {"schema": "public", "extend_existing": True}

    k_id_bicycle = Column(String(11), primary_key=True, index=True)