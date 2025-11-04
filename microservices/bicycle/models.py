import datetime as _dt
import sqlalchemy as _sql
import sqlalchemy.orm as _orm
import passlib.hash as _hash
from database import Base , engine_2
import database as _database

class Bicycle(_database.Base):
    __tablename__ = "bicycles"
    __table_args__ = {'schema': 'bicycle'}
    
    idBicileta = _sql.Column(_sql.Integer, primary_key=True, index=True)
    serie = _sql.Column(_sql.String)
    modelo = _sql.Column(_sql.String)
    estado = _sql.Column(_sql.String)

