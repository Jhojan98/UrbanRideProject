import sqlalchemy as _sql
from database import Base


class _StationReference(Base):
    """Register `public.station` so ForeignKey resolution works when creating metadata."""
    __tablename__ = "station"
    __table_args__ = {"schema": "public", "extend_existing": True}

    k_id_station = _sql.Column(_sql.Integer, primary_key=True, index=True)

class panic_button(Base):
    __tablename__ = "panic_button"
    __table_args__ = {"schema": "public"}

    k_id_panic_button = _sql.Column(_sql.Integer, primary_key=True, autoincrement=True)
    f_activation_date = _sql.Column(_sql.DateTime(timezone=False), nullable=False, server_default=_sql.func.now())
    k_id_station = _sql.Column('k_id_station', _sql.Integer, _sql.ForeignKey("public.station.k_id_station"), nullable=False)

    @property
    def id(self):
        return self.k_id_panic_button
