from datetime import date, datetime
from typing import Optional
from pydantic import BaseModel, Field, validator

class MetodoPagoBase(BaseModel):
    k_usuario_cc: str = Field(..., min_length=1)
    t_tipo_tarjeta: str = Field(..., description="CREDITO, DEBITO, PSE, EFECTIVO")
    n_nombre_titular: str
    f_fecha_expiracion: date
    n_direccion_facturacion: Optional[str] = None
    n_codigo_postal: Optional[str] = None
    b_principal: Optional[bool] = False

    @validator("t_tipo_tarjeta")
    def validar_tipo(cls, v):
        tipos = {"CREDITO", "DEBITO", "PSE", "EFECTIVO"}
        if v not in tipos:
            raise ValueError("Tipo de tarjeta inválido")
        return v

class MetodoPagoCreate(MetodoPagoBase):
    n_numero_tarjeta_completo: Optional[str] = None

class MetodoPagoUpdate(BaseModel):
    n_nombre_titular: Optional[str] = None
    f_fecha_expiracion: Optional[date] = None
    n_direccion_facturacion: Optional[str] = None
    n_codigo_postal: Optional[str] = None

class MetodoPagoOut(BaseModel):
    k_metodo_pago: int
    k_usuario_cc: str
    t_tipo_tarjeta: str
    n_numero_tarjeta: str
    n_nombre_titular: str
    f_fecha_expiracion: date
    n_marca: Optional[str]
    b_principal: bool
    b_activo: bool
    f_fecha_registro: Optional[datetime]
    n_direccion_facturacion: Optional[str]
    n_codigo_postal: Optional[str]
    v_saldo: int = 0

    class Config:
        from_attributes = True

class ValidacionTarjetaRequest(BaseModel):
    numeroTarjeta: str

class ValidacionTarjetaResponse(BaseModel):
    valido: bool

# ==================== Schemas para Recarga de Saldo ====================

class RecargaSaldoRequest(BaseModel):
    """Schema para solicitud de recarga de saldo"""
    k_metodo_pago: int = Field(..., description="ID del método de pago a recargar")
    monto: int = Field(..., gt=0, description="Monto a recargar (debe ser mayor a 0)")
    descripcion: Optional[str] = Field(None, description="Descripción de la recarga")
    
    @validator("monto")
    def validar_monto(cls, v):
        if v < 1000:
            raise ValueError("El monto mínimo de recarga es $1,000")
        if v > 5000000:
            raise ValueError("El monto máximo de recarga es $5,000,000")
        return v

class RecargaSaldoResponse(BaseModel):
    """Schema para respuesta de recarga de saldo"""
    k_metodo_pago: int
    k_usuario_cc: str
    monto_recargado: int
    saldo_anterior: int
    saldo_nuevo: int
    n_marca: Optional[str]
    n_numero_tarjeta: str
    fecha_recarga: datetime
    descripcion: Optional[str]
    mensaje: str

class ConsultaSaldoResponse(BaseModel):
    """Schema para consulta de saldo"""
    k_metodo_pago: int
    k_usuario_cc: str
    saldo_actual: int
    n_marca: Optional[str]
    n_numero_tarjeta: str
    ultima_actualizacion: Optional[datetime]
