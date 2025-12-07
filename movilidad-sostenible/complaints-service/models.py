import datetime as _dt
import sqlalchemy as _sql
import sqlalchemy.orm as _orm
from database import Base

class Complaint(Base):
    __tablename__ = "complaints_and_claims"
    __table_args__ = (
        _sql.CheckConstraint("t_status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')", name="CHK_t_status"),
     _sql.CheckConstraint("t_type IN ('BICYCLE', 'SLOT', 'STATION')", name="CHK_t_type")
    )

    k_id_complaints_and_claims = _sql.Column(_sql.Integer, primary_key=True, index=True, autoincrement=True)
    d_description = _sql.Column(_sql.String(500), nullable=False)
    t_status = _sql.Column(_sql.String(50), nullable=False, server_default='OPEN')
    k_id_travel = _sql.Column('k_id_travel', _sql.Integer, _sql.ForeignKey("public.travel.k_id_travel"), nullable=True)
    t_type = _sql.Column(_sql.String(50), nullable=False)

    @property
    def id(self):
        return self.k_id_complaints_and_claims
    
class _TravelReference(Base):
    """Register `public.travel` so ForeignKey resolution works when creating metadata."""
    __tablename__ = "travel"
    __table_args__ = {"schema": "public", "extend_existing": True}

    k_id_travel = _sql.Column(_sql.Integer, primary_key=True, index=True)