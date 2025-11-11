import datetime as _dt
import sqlalchemy as _sql
import sqlalchemy.orm as _orm
import passlib.hash as _hash
from database import Base

# Modelo alineado con la tabla public.usuario definida en 03-movilidad_sostenible_schema.sql
# Se mantiene el nombre de la clase "User" para no romper dependencias, pero __tablename__ apunta a "usuario".
class User(Base):
    __tablename__ = "users"
    k_user_cc = _sql.Column(_sql.Integer, primary_key=True, index=True, autoincrement=False)
    n_username = _sql.Column(_sql.String(50), unique=True, index=True, nullable=False)
    n_hashed_password = _sql.Column(_sql.String(128), nullable=False)
    n_user_first_name = _sql.Column(_sql.String(50), nullable=False)
    n_user_second_name = _sql.Column(_sql.String(50), nullable=True)
    n_user_first_lastname = _sql.Column(_sql.String(50), nullable=False)
    n_user_second_lastname = _sql.Column(_sql.String(50), nullable=True)
    f_user_birthdate = _sql.Column(_sql.Date, nullable=False)
    n_user_email = _sql.Column(_sql.String(100), unique=True, index=True, nullable=False)
    t_subscription_type = _sql.Column(_sql.String(50), default="NONE", nullable=False)
    f_user_registration_date = _sql.Column(_sql.String(50), nullable=False)
    t_is_verified = _sql.Column(_sql.Boolean, default=False, nullable=False)

    def verify_password(self, password: str):
        return _hash.bcrypt.verify(password, self.n_hashed_password)

class EmailVerification(Base):
    __tablename__ = "email_verification"
    k_id_email_verifiation = _sql.Column(_sql.Integer, primary_key=True)
    n_otp_hash = _sql.Column(_sql.String(255), nullable=False)
    n_user_email = _sql.Column(_sql.String(100), nullable=False, index=True)
    f_expires_at = _sql.Column(_sql.DateTime(timezone=False), nullable=False)
    t_consumed = _sql.Column(_sql.Boolean, nullable=False, default=False)
    f_created_at = _sql.Column(_sql.DateTime(timezone=False), nullable=False, default=_dt.datetime.utcnow)
    k_user_cc = _sql.Column(_sql.Integer, nullable=False, index=True)

