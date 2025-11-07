import datetime as _dt
import sqlalchemy as _sql
import sqlalchemy.orm as _orm
import passlib.hash as _hash
from database import Base , engine
import database as _database

class User(_database.Base):
    __tablename__ = "users"
    __table_args__ = {'schema': 'auth'}
    
    id = _sql.Column(_sql.Integer, primary_key=True, index=True)
    name = _sql.Column(_sql.String)
    email = _sql.Column(_sql.String, unique=True, index=True)
    is_verified = _sql.Column(_sql.Boolean , default=False)
    otp = _sql.Column(_sql.Integer)
    hashed_password = _sql.Column(_sql.String)
    addresses = _orm.relationship("Address", back_populates="user")
    date_created = _sql.Column(_sql.DateTime, default=_dt.datetime.utcnow)
    role = _sql.Column(_sql.String, default="user", nullable=False)

    def verify_password(self, password: str):
        return _hash.bcrypt.verify(password, self.hashed_password)

class Address(_database.Base):
    __tablename__ = "addresses"
    __table_args__ = {'schema': 'auth'}
    
    id = _sql.Column(_sql.Integer, primary_key=True, index=True)
    street = _sql.Column(_sql.String)
    landmark = _sql.Column(_sql.String)
    city = _sql.Column(_sql.String)
    country = _sql.Column(_sql.String)
    pincode = _sql.Column(_sql.String)
    user_id = _sql.Column(_sql.Integer, _sql.ForeignKey("auth.users.id"))
    user = _orm.relationship("User", back_populates="addresses")
    latitude = _sql.Column(_sql.Float)
    longitude = _sql.Column(_sql.Float)