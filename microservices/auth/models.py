import datetime as _dt
import sqlalchemy as _sql
import sqlalchemy.orm as _orm
from database import Base

class EmailVerification(Base):
    __tablename__ = "email_verification"
    k_id_email_verifiation = _sql.Column(_sql.Integer, primary_key=True)
    n_otp_hash = _sql.Column(_sql.String(255), nullable=False)
    n_user_email = _sql.Column(_sql.String(100), nullable=False, index=True)
    f_expires_at = _sql.Column(_sql.DateTime(timezone=False), nullable=False)
    t_consumed = _sql.Column(_sql.Boolean, nullable=False, default=False)
    f_created_at = _sql.Column(_sql.DateTime(timezone=False), nullable=False, default=_dt.datetime.utcnow)
    k_user_cc = _sql.Column(_sql.Integer, nullable=False, index=True)

