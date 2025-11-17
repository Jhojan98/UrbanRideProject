from sqlalchemy import Column, BigInteger, Integer, String, Boolean, Date, TIMESTAMP
from sqlalchemy.sql import func
from .database import Base

class MetodoPago(Base):
    """
    Payment Method Model - Maps to public.payment_method table
    Uses English column names in DB, Spanish property names in Python for consistency
    """
    __tablename__ = "payment_method"
    __table_args__ = {"schema": "public"}

    # Map Python (Spanish) to Database (English) column names
    k_metodo_pago = Column("k_id_payment_method", BigInteger, primary_key=True, autoincrement=True)
    k_usuario_cc = Column("k_user_cc", Integer, nullable=False)
    t_tipo_tarjeta = Column("t_card_type", String(20), nullable=False)  # CREDITO, DEBITO, PSE, EFECTIVO
    n_numero_tarjeta = Column("n_card_number", String(20), nullable=False)  # Masked number
    n_numero_tarjeta_completo = Column("n_card_number_full", String(20))  # Full number (demo only)
    n_nombre_titular = Column("n_owner_name", String(100), nullable=False)
    f_fecha_expiracion = Column("f_expiration_date", Date, nullable=False)
    n_marca = Column("n_brand", String(20))  # VISA, MASTERCARD, AMEX, etc.
    b_principal = Column("b_is_primary", Boolean, default=False)
    b_activo = Column("b_is_active", Boolean, default=True)
    f_fecha_registro = Column("f_registration_date", TIMESTAMP(timezone=False), server_default=func.now())
    n_direccion_facturacion = Column("n_billing_address", String(255))
    n_codigo_postal = Column("n_postal_code", String(20))
    v_saldo = Column("v_balance", BigInteger, default=0)  # Available balance (in cents)
