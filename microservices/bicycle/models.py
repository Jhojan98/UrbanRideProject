import datetime as _dt
import sqlalchemy as _sql
import sqlalchemy.orm as _orm
from database import Base

class Bicycle(Base):
    __tablename__ = "bicycle"
    __table_args__ = {"schema": "public"}

    k_id_bicycle = _sql.Column(_sql.Integer, primary_key=True, index=True, autoincrement=True)
    k_series = _sql.Column(_sql.Integer, primary_key=True, index=True)
    n_model = _sql.Column(_sql.String(50), nullable=False)
    t_padlock_status = _sql.Column(_sql.String(50), nullable=False, server_default='CLOSE')
    f_last_update = _sql.Column(_sql.DateTime(timezone=False))
    n_latitude = _sql.Column(_sql.Float)
    n_length = _sql.Column(_sql.Float)
    v_battery = _sql.Column(_sql.Numeric(5,2))

    @property
    def id(self):
        return self.k_id_bicycle

