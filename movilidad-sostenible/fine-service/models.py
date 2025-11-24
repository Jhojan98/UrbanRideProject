import datetime as _dt
import sqlalchemy as _sql
import sqlalchemy.orm as _orm
from database import Base


class _UsersReference(Base):
    """Register `public.users` so ForeignKey resolution works when creating metadata."""
    __tablename__ = "users"
    __table_args__ = {"schema": "public", "extend_existing": True}

    k_user_cc = _sql.Column(_sql.Integer, primary_key=True)

class UserFine(Base):
    __tablename__ = "user_fine"
    __table_args__ = (
        _sql.CheckConstraint("t_state IN ('PENDING', 'PAID', 'CANCELLED')", name="CHK_t_state"),
        {"schema": "public"},
    )

    k_user_fine = _sql.Column(_sql.Integer, primary_key=True, index=True, autoincrement=True)
    n_reason = _sql.Column(_sql.String(255), nullable=False)
    t_state = _sql.Column(_sql.String(50), nullable=False, server_default='PENDING')
    k_id_multa = _sql.Column(_sql.Integer, _sql.ForeignKey("public.fine.k_id_fine"), nullable=False)
    k_user_cc = _sql.Column(_sql.Integer, _sql.ForeignKey("public.users.k_user_cc"), nullable=False)
    v_amount_snapshot = _sql.Column(_sql.Numeric(9), nullable=False)
    f_assigned_at = _sql.Column(_sql.DateTime(timezone=False), nullable=False, server_default=_sql.func.now())
    f_updated_at = _sql.Column(
        _sql.DateTime(timezone=False),
        nullable=False,
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
    n_name = _sql.Column(_sql.String(120), nullable=False, server_default="Multa")
    d_description = _sql.Column(_sql.String(255), nullable=True)
    v_amount = _sql.Column(_sql.Numeric(9), nullable=False)

    user_fines = _orm.relationship("UserFine", back_populates="fine")
