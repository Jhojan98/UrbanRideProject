from sqlalchemy import Column, BigInteger, String, Boolean, Date, TIMESTAMP
from sqlalchemy.sql import func
from .database import Base

class MetodoPago(Base):
    __tablename__ = "metodo_pago"

    k_metodo_pago = Column("k_metodopago", BigInteger, primary_key=True, autoincrement=True)
    k_usuario_cc = Column("k_usuario_cc", String(50), nullable=False)
    t_tipo_tarjeta = Column("t_tipotarjeta", String(20), nullable=False)  # CREDITO, DEBITO, PSE, EFECTIVO
    n_numero_tarjeta = Column("n_numerotarjeta", String(20), nullable=False)  # número enmascarado
    n_numero_tarjeta_completo = Column("n_numerotarjetacompleto", String(20))  # ATENCIÓN: solo para demo
    n_nombre_titular = Column("n_nombretitular", String(100), nullable=False)
    f_fecha_expiracion = Column("f_fechaexpiracion", Date, nullable=False)
    n_marca = Column("n_marca", String(20))  # VISA, MASTERCARD, AMEX, etc.
    b_principal = Column("b_principal", Boolean, default=False)
    b_activo = Column("b_activo", Boolean, default=True)
    f_fecha_registro = Column("f_fecharegistro", TIMESTAMP(timezone=False), server_default=func.now())
    n_direccion_facturacion = Column("n_direccionfacturacion", String(255))
    n_codigo_postal = Column("n_codigopostal", String(20))
    v_saldo = Column("v_saldo", BigInteger, default=0)  # Saldo disponible en el método de pago
