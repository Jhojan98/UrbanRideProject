from sqlalchemy import Column, Date, ForeignKey, Integer, String, CheckConstraint
from database import Base


class MaintenanceRecord(Base):
    __tablename__ = "maintenance"
    __table_args__ = (
        CheckConstraint("t_entity_type IN ('BICYCLE', 'STATION', 'LOCK')", name="CHK_t_entity_type"),
        CheckConstraint("t_maintenance_type IN ('PREVENTIVE', 'CORRECTIVE', 'INSPECTION')", name="CHK_t_maintenace_type"),
        CheckConstraint("t_triggered_by IN ('ADMIN', 'USER', 'IOT_ALERT')", name="CHK_t_triggered_by"),
        CheckConstraint("t_status IN ('PENDING', 'SOLVING', 'RESOLVED')", name="CHK_t_status"),
    )

    k_id_maintenance = Column(String(50), primary_key=True, index=True)
    t_entity_type = Column(String(50), nullable=False)
    t_maintenance_type = Column(String(50), nullable=False)
    t_triggered_by = Column(String(50), nullable=False)
    d_description = Column(String(250), nullable=False)
    t_status = Column(String(50), nullable=False, server_default='PENDING')
    f_date = Column(Date, nullable=False)
    v_cost = Column(Integer, nullable=True)
    k_id_bicycle = Column(String(11), ForeignKey("public.bicycle.k_id_bicycle"), nullable=True)
    k_id_station = Column(Integer, ForeignKey("public.station.k_id_station"), nullable=True)
    k_id_slot = Column(String(50), ForeignKey("public.slots.k_id_slot"), nullable=True)

class __BicycleReference(Base):
    """Register `public.bicycle` so ForeignKey resolution works when creating metadata."""
    __tablename__ = "bicycle"
    __table_args__ = {"schema": "public", "extend_existing": True}

    k_id_bicycle = Column(String(11), primary_key=True, index=True)


class __StationReference(Base):
    """Minimal `public.station` registration for metadata synchronization."""
    __tablename__ = "station"
    __table_args__ = {"schema": "public", "extend_existing": True}

    k_id_station = Column(Integer, primary_key=True, index=True)


class __SlotReference(Base):
    """Minimal `public.slots` registration for metadata synchronization."""
    __tablename__ = "slots"
    __table_args__ = {"schema": "public", "extend_existing": True}

    k_id_slot = Column(String(50), primary_key=True, index=True)