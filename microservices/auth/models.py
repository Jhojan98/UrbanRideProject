import datetime as _dt
import sqlalchemy as _sql
import sqlalchemy.orm as _orm
import passlib.hash as _hash
from database import Base

# Modelo alineado con la tabla public.usuario definida en 03-movilidad_sostenible_schema.sql
# Se mantiene el nombre de la clase "User" para no romper dependencias, pero __tablename__ apunta a "usuario".
class User(Base):
    __tablename__ = "usuario"
    k_cedula_ciudadania_usuario = _sql.Column(_sql.Integer, primary_key=True, index=True)
    n_usuario = _sql.Column(_sql.String(50), unique=True, index=True, nullable=False)
    n_contrasena = _sql.Column(_sql.String(50), nullable=False)  # En el esquema es NOT NULL; almacenamos un placeholder seguro.
    hashed_password = _sql.Column(_sql.String(128), nullable=False)  # Password hasheado (bcrypt)
    is_verified = _sql.Column(_sql.Boolean, default=False, nullable=False)
    otp = _sql.Column(_sql.String(6), nullable=True)  # En el esquema es varchar(6)
    date_created = _sql.Column(_sql.DateTime(timezone=False), default=_dt.datetime.utcnow, nullable=False)
    n_primer_nombre = _sql.Column(_sql.String(50), nullable=False)
    n_segundo_nombre = _sql.Column(_sql.String(50), nullable=True)
    n_primer_apellido = _sql.Column(_sql.String(50), nullable=False)
    n_segundo_apellido = _sql.Column(_sql.String(50), nullable=True)
    f_fecha_nacimiento = _sql.Column(_sql.Date, nullable=False)
    n_correo_electronico = _sql.Column(_sql.String(100), unique=True, index=True, nullable=False)
    t_tipo_suscripcion = _sql.Column(_sql.String(50), default="NINGUNA", nullable=False)
    f_fecha_de_registro = _sql.Column(_sql.String(50), nullable=False)  # En el script es varchar(50)

    def verify_password(self, password: str):
        return _hash.bcrypt.verify(password, self.hashed_password)

# Tablas de roles
class Rol(Base):
    __tablename__ = "rol"
    k_id_rol = _sql.Column(_sql.Integer, primary_key=True)
    n_nombre = _sql.Column(_sql.String(50), unique=True, nullable=False)

class UsuarioRol(Base):
    __tablename__ = "usuario_rol"
    k_cedula_ciudadania_usuario = _sql.Column(_sql.Integer, primary_key=True)
    k_id_rol = _sql.Column(_sql.Integer, primary_key=True)

