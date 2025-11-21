from sqlalchemy import Column, Integer, TIMESTAMP
from sqlalchemy.sql import func
from datetime import datetime
from .database import Base

class Travel(Base):
    """
    Travel/Reservation Model - Maps to public.travel table
    """
    __tablename__ = "travel"
    __table_args__ = {"schema": "public"}

    k_id_travel = Column("k_id_travel", Integer, primary_key=True, autoincrement=True)
    f_request_date = Column("f_request_date", TIMESTAMP(timezone=False), default=datetime.now, nullable=True)
    k_user_cc = Column("k_user_cc", Integer, nullable=False)
    k_series = Column("k_series", Integer, nullable=False)
    k_id_bicycle = Column("k_id_bicycle", Integer, nullable=False)
