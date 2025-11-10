import datetime as _dt
import sqlalchemy as _sql
import sqlalchemy.orm as _orm
from database import Base

class Bicycle(Base):
    __tablename__ = "bicicleta"
    __table_args__ = {"schema": "public"}

    k_id_bicicleta = _sql.Column(_sql.Integer, primary_key=True, index=True, autoincrement=True)
    k_serie = _sql.Column(_sql.Integer, index=True, nullable=True)
    n_modelo = _sql.Column(_sql.String(50), nullable=False)
    t_estado_candado = _sql.Column(_sql.String(50), nullable=False, server_default='CERRADO')
    f_ultima_actualizacion = _sql.Column(_sql.DateTime(timezone=False))
    n_latitud = _sql.Column(_sql.Float)
    n_longitud = _sql.Column(_sql.Float)
    v_bateria = _sql.Column(_sql.Numeric(5,2))

    @property
    def id(self):
        return self.k_id_bicicleta

