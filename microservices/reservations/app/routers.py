from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import select
from . import models, schemas
from .database import get_db
import requests
import os

router = APIRouter(prefix="/api/reservations", tags=["reservations"])

# Configuración
PAYMENT_SERVICE_URL = os.getenv("PAYMENT_SERVICE_URL", "http://payment-service:5003")
COSTO_RESERVA = 5000  # Costo fijo por reserva en pesos

@router.post("/", response_model=schemas.TravelOut, status_code=201)
def crear_reserva(data: schemas.TravelCreate, db: Session = Depends(get_db)):
    """
    Crear una nueva reserva/viaje con cobro automático.
    
    - Valida que el usuario tenga saldo suficiente
    - Descuenta el costo de la reserva del método de pago
    - Crea la reserva
    """
    
    # 1. Consultar saldo del método de pago
    try:
        balance_response = requests.get(
            f"{PAYMENT_SERVICE_URL}/api/metodos-pago/saldo/{data.k_metodo_pago}"
        )
        if balance_response.status_code != 200:
            raise HTTPException(
                status_code=404, 
                detail="Método de pago no encontrado"
            )
        
        balance_data = balance_response.json()
        saldo_actual = balance_data.get("saldo_actual", 0)
        
        # Verificar que el método de pago pertenece al usuario
        if balance_data.get("k_usuario_cc") != data.k_user_cc:
            raise HTTPException(
                status_code=403,
                detail="El método de pago no pertenece al usuario"
            )
        
    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=503,
            detail="Servicio de pagos no disponible"
        )
    
    # 2. Validar saldo suficiente
    if saldo_actual < COSTO_RESERVA:
        raise HTTPException(
            status_code=400,
            detail=f"Saldo insuficiente. Requiere ${COSTO_RESERVA:,}, disponible: ${saldo_actual:,}"
        )
    
    # 3. Descontar el costo de la reserva
    try:
        # Llamar al endpoint interno del payment service para descontar
        deduct_response = requests.post(
            f"{PAYMENT_SERVICE_URL}/api/metodos-pago/{data.k_metodo_pago}/descontar",
            json={"monto": COSTO_RESERVA, "descripcion": "Pago por reserva de bicicleta"}
        )
        
        if deduct_response.status_code != 200:
            raise HTTPException(
                status_code=400,
                detail="Error al procesar el pago"
            )
            
    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=503,
            detail="Servicio de pagos no disponible"
        )
    
    # 4. Crear la reserva
    travel = models.Travel(
        k_user_cc=data.k_user_cc,
        k_series=data.k_series,
        k_id_bicycle=data.k_id_bicycle
    )
    db.add(travel)
    db.commit()
    db.refresh(travel)
    
    return travel

@router.get("/user/{user_cc}", response_model=list[schemas.TravelOut])
def listar_reservas_usuario(user_cc: int, db: Session = Depends(get_db)):
    """
    Obtener todas las reservas de un usuario.
    """
    stmt = select(models.Travel).where(
        models.Travel.k_user_cc == user_cc
    ).order_by(models.Travel.f_request_date.desc())
    
    travels = db.execute(stmt).scalars().all()
    return travels

@router.get("/{travel_id}", response_model=schemas.TravelOut)
def obtener_reserva(travel_id: int, db: Session = Depends(get_db)):
    """
    Obtener una reserva específica por ID.
    """
    travel = db.get(models.Travel, travel_id)
    if not travel:
        raise HTTPException(status_code=404, detail="Reserva no encontrada")
    return travel

@router.get("/{travel_id}/user/{user_cc}", response_model=schemas.TravelOut)
def obtener_reserva_usuario(travel_id: int, user_cc: int, db: Session = Depends(get_db)):
    """
    Obtener una reserva específica de un usuario.
    """
    travel = db.get(models.Travel, travel_id)
    if not travel or travel.k_user_cc != user_cc:
        raise HTTPException(status_code=404, detail="Reserva no encontrada")
    return travel

@router.delete("/{travel_id}/user/{user_cc}", status_code=200)
def cancelar_reserva(travel_id: int, user_cc: int, db: Session = Depends(get_db)):
    """
    Cancelar (eliminar) una reserva.
    """
    travel = db.get(models.Travel, travel_id)
    if not travel or travel.k_user_cc != user_cc:
        raise HTTPException(status_code=404, detail="Reserva no encontrada")
    
    db.delete(travel)
    db.commit()
    
    return {"mensaje": "Reserva cancelada correctamente"}

@router.get("/bicycle/{series}/{bicycle_id}", response_model=list[schemas.TravelOut])
def listar_reservas_bicicleta(series: int, bicycle_id: int, db: Session = Depends(get_db)):
    """
    Obtener todas las reservas de una bicicleta específica.
    """
    stmt = select(models.Travel).where(
        models.Travel.k_series == series,
        models.Travel.k_id_bicycle == bicycle_id
    ).order_by(models.Travel.f_request_date.desc())
    
    travels = db.execute(stmt).scalars().all()
    return travels
