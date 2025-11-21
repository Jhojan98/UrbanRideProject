from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import select
from datetime import datetime, timedelta
from . import models, schemas
from .database import get_db
import requests
import os

router = APIRouter(prefix="/api/reservations", tags=["reservations"])

# Configuración
PAYMENT_SERVICE_URL = os.getenv("PAYMENT_SERVICE_URL", "http://payment-service:5006")
COSTO_RESERVA = 5000  # Costo fijo por reserva en pesos

@router.post("/", response_model=schemas.TravelOut, status_code=201)
def crear_reserva(data: schemas.TravelCreate, db: Session = Depends(get_db)):
    """
    Crear una nueva reserva/viaje con cobro automático.
    
    - Valida que el usuario tenga saldo suficiente
    - Descuenta el costo de la reserva del método de pago
    - Crea la reserva
    """
    # Reglas de negocio básicas con ventana de 10 minutos
    ventana_minutos = 10
    ahora = datetime.now()
    limite = ahora - timedelta(minutes=ventana_minutos)

    # 0. Verificar si el usuario ya tiene una reserva activa reciente
    stmt_usuario = select(models.Travel).where(
        models.Travel.k_user_cc == data.k_user_cc,
        models.Travel.f_request_date >= limite,
    )
    reserva_usuario = db.execute(stmt_usuario).scalars().first()
    if reserva_usuario:
        raise HTTPException(
            status_code=400,
            detail="El usuario ya tiene una reserva activa reciente. Debe esperar antes de realizar otra reserva.",
        )

    # 0.b Verificar si la bicicleta ya está reservada en la ventana de tiempo
    stmt_bici = select(models.Travel).where(
        models.Travel.k_series == data.k_series,
        models.Travel.k_id_bicycle == data.k_id_bicycle,
        models.Travel.f_request_date >= limite,
    )
    reserva_bici = db.execute(stmt_bici).scalars().first()
    if reserva_bici:
        raise HTTPException(
            status_code=400,
            detail="La bicicleta ya se encuentra reservada actualmente.",
        )

    # 1. Validar que el método de pago exista y pertenezca al usuario
    try:
        metodo_response = requests.get(
            f"{PAYMENT_SERVICE_URL}/api/metodos-pago/{data.k_metodo_pago}/usuario/{data.k_user_cc}"
        )
    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=503,
            detail="Servicio de pagos no disponible"
        )

    if metodo_response.status_code != 200:
        try:
            error_data = metodo_response.json()
        except ValueError:
            error_data = {"detail": "Error al validar el método de pago"}
        raise HTTPException(
            status_code=metodo_response.status_code,
            detail=error_data.get("detail", error_data),
        )

    # 2. Crear PaymentIntent en Stripe por el costo de la reserva
    intent_body = {
        "k_metodo_pago": data.k_metodo_pago,
        "monto": COSTO_RESERVA,
        "moneda": None,
        "tipo_operacion": "reserva_bicicleta",
        "k_series": data.k_series,
        "k_id_bicycle": data.k_id_bicycle,
    }

    try:
        intent_response = requests.post(
            f"{PAYMENT_SERVICE_URL}/api/metodos-pago/stripe/payment-intent",
            json=intent_body,
        )
    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=503,
            detail="Servicio de pagos no disponible"
        )

    if intent_response.status_code != 200:
        try:
            intent_error = intent_response.json()
        except ValueError:
            intent_error = {"detail": "Error al procesar el pago con Stripe"}
        raise HTTPException(
            status_code=intent_response.status_code,
            detail=intent_error.get("detail", intent_error),
        )

    # 3. Crear la reserva
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
