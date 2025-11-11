from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .database import get_db
from . import models, schemas
from .utils import luhn_valid, detectar_marca, mask
from sqlalchemy import select, update, func
from datetime import date, datetime
from .messaging import (
    publish_metodo_pago_created,
    publish_metodo_pago_updated,
    publish_metodo_pago_deleted,
    publish_metodo_pago_set_principal,
    publish_saldo_recargado
)

router = APIRouter(prefix="/api/metodos-pago", tags=["metodos-pago"])

@router.get("/usuario/{usuario_cc}", response_model=list[schemas.MetodoPagoOut])
def listar(usuario_cc: int, db: Session = Depends(get_db)):
    stmt = select(models.MetodoPago).where(
        models.MetodoPago.k_usuario_cc == usuario_cc,
        models.MetodoPago.b_activo == True
    )
    return db.execute(stmt).scalars().all()

@router.get("/{id}/usuario/{usuario_cc}", response_model=schemas.MetodoPagoOut)
def obtener(id: int, usuario_cc: int, db: Session = Depends(get_db)):
    mp = db.get(models.MetodoPago, id)
    if not mp or mp.k_usuario_cc != usuario_cc:
        raise HTTPException(status_code=404, detail="Método de pago no encontrado")
    return mp

@router.get("/usuario/{usuario_cc}/principal", response_model=schemas.MetodoPagoOut)
def principal(usuario_cc: int, db: Session = Depends(get_db)):
    stmt = select(models.MetodoPago).where(
        models.MetodoPago.k_usuario_cc == usuario_cc,
        models.MetodoPago.b_principal == True,
        models.MetodoPago.b_activo == True
    )
    mp = db.execute(stmt).scalars().first()
    if not mp:
        raise HTTPException(status_code=404, detail="No hay método principal")
    return mp

@router.post("/", response_model=schemas.MetodoPagoOut, status_code=201)
async def crear(data: schemas.MetodoPagoCreate, db: Session = Depends(get_db)):
    if data.n_numero_tarjeta_completo and not luhn_valid(data.n_numero_tarjeta_completo):
        raise HTTPException(status_code=400, detail="Número de tarjeta inválido")
    if data.f_fecha_expiracion <= date.today():
        raise HTTPException(status_code=400, detail="La tarjeta está vencida")

    masked = mask(data.n_numero_tarjeta_completo) if data.n_numero_tarjeta_completo else ""
    marca = detectar_marca(data.n_numero_tarjeta_completo or "")

    # Si no hay métodos activos del usuario, forzar principal
    count = db.scalar(
        select(func.count()).select_from(models.MetodoPago).where(
            models.MetodoPago.k_usuario_cc == data.k_usuario_cc,
            models.MetodoPago.b_activo == True
        )
    ) or 0
    es_principal = data.b_principal or (count == 0)
    if es_principal:
        db.execute(
            update(models.MetodoPago)
            .where(models.MetodoPago.k_usuario_cc == data.k_usuario_cc)
            .values(b_principal=False)
        )

    mp = models.MetodoPago(
        k_usuario_cc=data.k_usuario_cc,
        t_tipo_tarjeta=data.t_tipo_tarjeta,
        n_numero_tarjeta=masked,
        n_numero_tarjeta_completo=data.n_numero_tarjeta_completo,
        n_nombre_titular=data.n_nombre_titular,
        f_fecha_expiracion=data.f_fecha_expiracion,
        n_marca=marca,
        b_principal=es_principal,
        b_activo=True,
        n_direccion_facturacion=data.n_direccion_facturacion,
        n_codigo_postal=data.n_codigo_postal,
    )
    db.add(mp)
    db.commit()
    db.refresh(mp)
    
    # Publicar evento de creación
    await publish_metodo_pago_created({
        "k_metodo_pago": mp.k_metodo_pago,
        "k_usuario_cc": mp.k_usuario_cc,
        "t_tipo_tarjeta": mp.t_tipo_tarjeta,
        "n_marca": mp.n_marca,
        "b_principal": mp.b_principal
    })
    
    return mp

@router.put("/{id}/usuario/{usuario_cc}", response_model=schemas.MetodoPagoOut)
async def actualizar(id: int, usuario_cc: int, data: schemas.MetodoPagoUpdate, db: Session = Depends(get_db)):
    mp = db.get(models.MetodoPago, id)
    if not mp or mp.k_usuario_cc != usuario_cc:
        raise HTTPException(status_code=404, detail="Método de pago no encontrado")
    if data.f_fecha_expiracion and data.f_fecha_expiracion <= date.today():
        raise HTTPException(status_code=400, detail="La tarjeta está vencida")
    if data.n_nombre_titular is not None:
        mp.n_nombre_titular = data.n_nombre_titular
    if data.f_fecha_expiracion is not None:
        mp.f_fecha_expiracion = data.f_fecha_expiracion
    if data.n_direccion_facturacion is not None:
        mp.n_direccion_facturacion = data.n_direccion_facturacion
    if data.n_codigo_postal is not None:
        mp.n_codigo_postal = data.n_codigo_postal
    db.commit()
    db.refresh(mp)
    
    # Publicar evento de actualización
    await publish_metodo_pago_updated({
        "k_metodo_pago": mp.k_metodo_pago,
        "k_usuario_cc": mp.k_usuario_cc,
        "cambios_realizados": True
    })
    
    return mp

@router.delete("/{id}/usuario/{usuario_cc}", status_code=200)
async def eliminar(id: int, usuario_cc: int, db: Session = Depends(get_db)):
    mp = db.get(models.MetodoPago, id)
    if not mp or mp.k_usuario_cc != usuario_cc:
        raise HTTPException(status_code=404, detail="Método de pago no encontrado")
    era_principal = bool(mp.b_principal)
    mp.b_activo = False
    mp.b_principal = False
    db.commit()

    if era_principal:
        # Promover otro método activo
        stmt = select(models.MetodoPago).where(
            models.MetodoPago.k_usuario_cc == usuario_cc,
            models.MetodoPago.b_activo == True
        ).limit(1)
        nuevo = db.execute(stmt).scalars().first()
        if nuevo:
            nuevo.b_principal = True
            db.commit()
    
    # Publicar evento de eliminación
    await publish_metodo_pago_deleted(id, usuario_cc)
    
    return {"mensaje": "Método de pago eliminado correctamente"}

@router.patch("/{id}/usuario/{usuario_cc}/principal", response_model=schemas.MetodoPagoOut)
async def set_principal(id: int, usuario_cc: int, db: Session = Depends(get_db)):
    mp = db.get(models.MetodoPago, id)
    if not mp or mp.k_usuario_cc != usuario_cc:
        raise HTTPException(status_code=404, detail="Método de pago no encontrado")
    if not mp.b_activo:
        raise HTTPException(status_code=400, detail="Método inactivo")

    db.execute(
        update(models.MetodoPago)
        .where(models.MetodoPago.k_usuario_cc == usuario_cc)
        .values(b_principal=False)
    )
    mp.b_principal = True
    db.commit()
    db.refresh(mp)
    
    # Publicar evento de cambio de método principal
    await publish_metodo_pago_set_principal({
        "k_metodo_pago": mp.k_metodo_pago,
        "k_usuario_cc": mp.k_usuario_cc,
        "n_marca": mp.n_marca
    })
    
    return mp

@router.post("/validar", response_model=schemas.ValidacionTarjetaResponse)
def validar(req: schemas.ValidacionTarjetaRequest):
    return schemas.ValidacionTarjetaResponse(valido=luhn_valid(req.numeroTarjeta))


# ==================== Endpoints de Recarga de Saldo ====================

@router.post("/recarga", response_model=schemas.RecargaSaldoResponse, status_code=200)
async def recargar_saldo(data: schemas.RecargaSaldoRequest, db: Session = Depends(get_db)):
    """
    Recarga de saldo MOCK para método de pago.
    
    Given: Usuario logueado
    When: Ingresa monto de recarga y método de pago
    Then: Sistema actualiza y muestra nuevo saldo
    """
    # Obtener método de pago
    mp = db.get(models.MetodoPago, data.k_metodo_pago)
    if not mp:
        raise HTTPException(status_code=404, detail="Método de pago no encontrado")
    
    if not mp.b_activo:
        raise HTTPException(status_code=400, detail="El método de pago está inactivo")
    
    # Guardar saldo anterior
    saldo_anterior = mp.v_saldo or 0
    
    # Simular proceso de recarga (MOCK)
    # En producción aquí se integraría con pasarela de pago real
    nuevo_saldo = saldo_anterior + data.monto
    
    # Actualizar saldo
    mp.v_saldo = nuevo_saldo
    db.commit()
    db.refresh(mp)
    
    # Crear respuesta
    fecha_actual = datetime.now()
    response = schemas.RecargaSaldoResponse(
        k_metodo_pago=mp.k_metodo_pago,
        k_usuario_cc=mp.k_usuario_cc,
        monto_recargado=data.monto,
        saldo_anterior=saldo_anterior,
        saldo_nuevo=nuevo_saldo,
        n_marca=mp.n_marca,
        n_numero_tarjeta=mp.n_numero_tarjeta,
        fecha_recarga=fecha_actual,
        descripcion=data.descripcion,
        mensaje=f"Recarga exitosa de ${data.monto:,}. Nuevo saldo: ${nuevo_saldo:,}"
    )
    
    # Publicar evento de recarga
    await publish_saldo_recargado({
        "k_metodo_pago": mp.k_metodo_pago,
        "k_usuario_cc": mp.k_usuario_cc,
        "monto_recargado": data.monto,
        "saldo_anterior": saldo_anterior,
        "saldo_nuevo": nuevo_saldo,
        "n_marca": mp.n_marca,
        "fecha_recarga": str(fecha_actual)
    })
    
    return response


@router.get("/saldo/{metodo_pago_id}", response_model=schemas.ConsultaSaldoResponse)
def consultar_saldo(metodo_pago_id: int, db: Session = Depends(get_db)):
    """
    Consulta el saldo actual de un método de pago.
    """
    mp = db.get(models.MetodoPago, metodo_pago_id)
    if not mp:
        raise HTTPException(status_code=404, detail="Método de pago no encontrado")
    
    if not mp.b_activo:
        raise HTTPException(status_code=400, detail="El método de pago está inactivo")
    
    return schemas.ConsultaSaldoResponse(
        k_metodo_pago=mp.k_metodo_pago,
        k_usuario_cc=mp.k_usuario_cc,
        saldo_actual=mp.v_saldo or 0,
        n_marca=mp.n_marca,
        n_numero_tarjeta=mp.n_numero_tarjeta,
        ultima_actualizacion=mp.f_fecha_registro
    )


@router.get("/usuario/{usuario_cc}/saldo-total", response_model=dict)
def consultar_saldo_total(usuario_cc: int, db: Session = Depends(get_db)):
    """
    Consulta el saldo total de todos los métodos de pago activos del usuario.
    """
    stmt = select(models.MetodoPago).where(
        models.MetodoPago.k_usuario_cc == usuario_cc,
        models.MetodoPago.b_activo == True
    )
    metodos = db.execute(stmt).scalars().all()
    
    if not metodos:
        raise HTTPException(status_code=404, detail="No se encontraron métodos de pago activos")
    
    saldo_total = sum(mp.v_saldo or 0 for mp in metodos)
    metodos_con_saldo = [
        {
            "k_metodo_pago": mp.k_metodo_pago,
            "n_marca": mp.n_marca,
            "n_numero_tarjeta": mp.n_numero_tarjeta,
            "saldo": mp.v_saldo or 0,
            "b_principal": mp.b_principal
        }
        for mp in metodos
    ]
    
    return {
        "k_usuario_cc": usuario_cc,
        "saldo_total": saldo_total,
        "cantidad_metodos": len(metodos),
        "metodos_pago": metodos_con_saldo
    }


@router.post("/{metodo_pago_id}/descontar", status_code=200)
def descontar_saldo(metodo_pago_id: int, data: dict, db: Session = Depends(get_db)):
    """
    Descuenta un monto del saldo de un método de pago.
    Usado internamente por el servicio de reservas.
    
    Args:
        metodo_pago_id: ID del método de pago
        data: {"monto": int, "descripcion": str}
    """
    monto = data.get("monto", 0)
    descripcion = data.get("descripcion", "Descuento")
    
    if monto <= 0:
        raise HTTPException(status_code=400, detail="El monto debe ser mayor a 0")
    
    # Obtener método de pago
    mp = db.get(models.MetodoPago, metodo_pago_id)
    if not mp:
        raise HTTPException(status_code=404, detail="Método de pago no encontrado")
    
    if not mp.b_activo:
        raise HTTPException(status_code=400, detail="El método de pago está inactivo")
    
    # Verificar saldo suficiente
    saldo_actual = mp.v_saldo or 0
    if saldo_actual < monto:
        raise HTTPException(
            status_code=400,
            detail=f"Saldo insuficiente. Disponible: ${saldo_actual:,}, requerido: ${monto:,}"
        )
    
    # Descontar
    nuevo_saldo = saldo_actual - monto
    mp.v_saldo = nuevo_saldo
    db.commit()
    db.refresh(mp)
    
    return {
        "k_metodo_pago": mp.k_metodo_pago,
        "k_usuario_cc": mp.k_usuario_cc,
        "monto_descontado": monto,
        "saldo_anterior": saldo_actual,
        "saldo_nuevo": nuevo_saldo,
        "descripcion": descripcion,
        "mensaje": f"Descuento exitoso de ${monto:,}. Nuevo saldo: ${nuevo_saldo:,}"
    }
