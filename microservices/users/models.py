import sqlalchemy as _sql
import sqlalchemy.orm as _orm
from database import Base

class User(Base):
    __tablename__ = "users"
    k_user_cc = _sql.Column(_sql.Integer, primary_key=True, autoincrement=False)
    n_username = _sql.Column(_sql.String(50), unique=True, index=True, nullable=False)
    n_hashed_password = _sql.Column(_sql.String(128), nullable=False)
    n_user_first_name = _sql.Column(_sql.String(50), nullable=False)
    n_user_second_name = _sql.Column(_sql.String(50), nullable=True)
    n_user_first_lastname = _sql.Column(_sql.String(50), nullable=False)
    n_user_second_lastname = _sql.Column(_sql.String(50), nullable=True)
    f_user_birthdate = _sql.Column(_sql.Date, nullable=False)
    n_user_email = _sql.Column(_sql.String(100), unique=True, index=True, nullable=False)
    t_subscription_type = _sql.Column(_sql.String(50), default="NONE", nullable=False)
    f_user_registration_date = _sql.Column(_sql.Date, nullable=False)
    t_is_verified = _sql.Column(_sql.Boolean, default=False, nullable=False)
