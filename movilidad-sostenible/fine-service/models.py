import datetime as _dt
import sqlalchemy as _sql
import sqlalchemy.orm as _orm
from database import Base


class _UsersReference(Base):
    """Register `public.users` so ForeignKey resolution works when creating metadata."""
    __tablename__ = "users"
    __table_args__ = {"schema": "public", "extend_existing": True}

    k_uid_user = _sql.Column(_sql.String(100), primary_key=True, index=True)

class UserFine(Base):
    __tablename__ = "user_fine"
    __table_args__ = (
        _sql.CheckConstraint("t_state IN ('PENDING', 'PAID')", name="CHK_t_state"),
        {"schema": "public"},
    )

    k_user_fine = _sql.Column(_sql.Integer, primary_key=True, index=True, autoincrement=True)
    n_reason = _sql.Column(_sql.String(250), nullable=False)
    t_state = _sql.Column(_sql.String(50), nullable=False, server_default='PENDING')
    k_id_fine = _sql.Column('k_id_fine', _sql.Integer, _sql.ForeignKey("public.fine.k_id_fine"), nullable=False)
    k_uid_user = _sql.Column('k_uid_user', _sql.String(100), _sql.ForeignKey("public.users.k_uid_user"), nullable=True)
    v_amount_snapshot = _sql.Column(_sql.Integer, nullable=True)
    f_assigned_at = _sql.Column(_sql.DateTime(timezone=False), nullable=False, server_default=_sql.func.now())
    f_update_at = _sql.Column(
        'f_update_at',
        _sql.DateTime(timezone=False),
        nullable=True,
        server_default=_sql.func.now(),
        onupdate=_sql.func.now(),
    )

    fine = _orm.relationship("Fine", back_populates="user_fines")

    @property
    def id(self):
        return self.k_user_fine
    
class Fine(Base):
    __tablename__ = "fine"
    __table_args__ = {"schema": "public"}

    k_id_fine = _sql.Column(_sql.Integer, primary_key=True, autoincrement=True)
    d_description = _sql.Column(_sql.String(250), nullable=False)
    v_amount = _sql.Column(_sql.Integer, nullable=False)

    user_fines = _orm.relationship("UserFine", back_populates="fine")
