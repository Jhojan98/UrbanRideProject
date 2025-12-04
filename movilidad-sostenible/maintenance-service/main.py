from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
import os
import database
import models
import schemas
from contextlib import asynccontextmanager
import logging
from eureka import start as start_eureka, stop as stop_eureka
import httpx
@asynccontextmanager
async def lifespan(_: FastAPI):
    eureka_handle = await start_eureka()
    try:
        yield
    finally:
        await stop_eureka(eureka_handle)


logging.basicConfig(level=logging.INFO)
models.Base.metadata.create_all(bind=database.engine)

app = FastAPI(lifespan=lifespan, title="Mantenimiento Service")

def _get_bicycle_service_base_url() -> str:
    bicycle_url= os.getenv("BICYCLE_SERVICE_URL", "http://bicis-service:8002")
    print(f"Using Bicycle service URL: {bicycle_url}")
    bicycle_url = bicycle_url.strip()
    if bicycle_url and not bicycle_url.startswith("http://") and not bicycle_url.startswith("https://"):
        bicycle_url = "http://" + bicycle_url
    return bicycle_url.rstrip("/")


def _get_station_service_base_url() -> str:
    """Return Station service base URL """
    station_url = os.getenv("STATION_SERVICE_URL", "http://stations-service:8003")
    print(f"Using Station service URL: {station_url}")
    station_url = station_url.strip()
    if station_url and not station_url.startswith("http://") and not station_url.startswith("https://"):
        station_url = "http://" + station_url
    return station_url.rstrip("/")


def _get_lock_service_base_url() -> str:
    """Return Lock service base URL """
    lock_url = os.getenv("LOCK_SERVICE_URL", "http://locks-service:8004")
    print(f"Using Lock service URL: {lock_url}")
    lock_url = lock_url.strip()
    if lock_url and not lock_url.startswith("http://") and not lock_url.startswith("https://"):
        lock_url = "http://" + lock_url
    return lock_url.rstrip("/")

async def _ensure_bicycle_exists(bike_id: str) -> dict:
    if not bike_id:
        raise HTTPException(status_code=400, detail="Bike ID is required for bicycle maintenance records.")
    base_url = _get_bicycle_service_base_url()
    endpoint = f"{base_url}/{bike_id}"
    print(f"Checking bicycle existence at {endpoint}")
    try:
        print("1. Creating client...")
        async with httpx.AsyncClient(timeout=5.0) as client:
            print("2. Sending request...")
            resp = await client.get(endpoint)
            print(f"3. Response received. Status code: {resp.status_code}")
            print(f"4. Response body: {resp.text}")
            if resp.status_code == 200:
                return resp.json()
            if resp.status_code == 404:
                raise HTTPException(status_code=400, detail=f"Bicycle with ID {bike_id} does not exist.")
            resp.raise_for_status()
            raise HTTPException(status_code=502, detail = "Bicycle service error.")
    except httpx.RequestError as e:
        logging.error(f"Error connecting to Bicycle service: {e}")
        raise HTTPException(status_code=503, detail="Unable to connect to Bicycle service.")


async def _ensure_station_exists(station_id: int) -> dict:
    if station_id is None:
        raise HTTPException(status_code=400, detail="Station ID is required for station maintenance records.")
    base_url = _get_station_service_base_url()
    endpoint = f"{base_url}/stations/{station_id}"
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            resp = await client.get(endpoint)
            if resp.status_code == 200:
                return resp.json()
            if resp.status_code == 404:
                raise HTTPException(status_code=400, detail=f"Station with ID {station_id} does not exist.")
            resp.raise_for_status()
            raise HTTPException(status_code=502, detail="Station service error.")
    except httpx.RequestError as e:
        logging.error(f"Error connecting to Station service: {e}")
        raise HTTPException(status_code=503, detail="Unable to connect to Station service.")


async def _ensure_lock_exists(lock_id: str) -> dict:
    if not lock_id:
        raise HTTPException(status_code=400, detail="Lock ID is required for lock maintenance records.")
    base_url = _get_lock_service_base_url()
    endpoint = f"{base_url}/{lock_id}"
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            resp = await client.get(endpoint)
            if resp.status_code == 200:
                return resp.json()
            if resp.status_code == 404:
                raise HTTPException(status_code=400, detail=f"Lock with ID {lock_id} does not exist.")
            resp.raise_for_status()
            raise HTTPException(status_code=502, detail="Lock service error.")
    except httpx.RequestError as e:
        logging.error(f"Error connecting to Lock service: {e}")
        raise HTTPException(status_code=503, detail="Unable to connect to Lock service.")


def _validate_entity_reference(record: schemas.MaintenanceRecordBase) -> None:
    entity = record.t_entity_tipe
    target_field = {
        "BICYCLE": record.k_id_bicycle,
        "STATION": record.k_id_station,
        "LOCK": record.k_id_lock,
    }
    required_value = target_field.get(entity)
    if not required_value:
        raise HTTPException(
            status_code=400,
            detail=f"{entity.title()} maintenance records require a valid identifier.",
        )


def _normalize_entity_payload(payload: dict, entity: str) -> dict:
    if entity != "BICYCLE":
        payload["k_id_bicycle"] = None
    if entity != "STATION":
        payload["k_id_station"] = None
    if entity != "LOCK":
        payload["k_id_lock"] = None
    return payload

@app.post("/maintenance/", response_model=schemas.MaintenanceRecord)
async def create_maintenance_record(record: schemas.MaintenanceRecordCreate, db: Session = Depends(database.get_db)):
    _validate_entity_reference(record)
    if record.t_entity_tipe == "BICYCLE":
        await _ensure_bicycle_exists(record.k_id_bicycle)
    elif record.t_entity_tipe == "STATION":
        await _ensure_station_exists(record.k_id_station)
    elif record.t_entity_tipe == "LOCK":
        await _ensure_lock_exists(record.k_id_lock)
    payload = record.dict(by_alias=False)
    payload = _normalize_entity_payload(payload, record.t_entity_tipe)
    db_record = models.MaintenanceRecord(**payload)
    db.add(db_record)
    db.commit()
    db.refresh(db_record)
    return db_record

@app.get("/maintenance/", response_model=List[schemas.MaintenanceRecord])
def read_maintenance_records(skip: int = 0, limit: int = 100, db: Session = Depends(database.get_db)):
    records = db.query(models.MaintenanceRecord).offset(skip).limit(limit).all()
    return records

@app.get("/maintenance/{maintenance_id}", response_model=schemas.MaintenanceRecord)
def read_maintenance_record(maintenance_id: int, db: Session = Depends(database.get_db)):
    record = db.query(models.MaintenanceRecord).filter(models.MaintenanceRecord.k_id_maintenance == maintenance_id).first()
    if record is None:
        raise HTTPException(status_code=404, detail="Maintenance record not found")
    return record

@app.put("/maintenance/{maintenance_id}", response_model=schemas.MaintenanceRecord)
async def update_maintenance_record(maintenance_id: int, record: schemas.MaintenanceRecordCreate, db: Session = Depends(database.get_db)):
    db_record = db.query(models.MaintenanceRecord).filter(models.MaintenanceRecord.k_id_maintenance == maintenance_id).first()
    if db_record is None:
        raise HTTPException(status_code=404, detail="Maintenance record not found")
    
    _validate_entity_reference(record)
    if record.t_entity_tipe == "BICYCLE":
        await _ensure_bicycle_exists(record.k_id_bicycle)
    elif record.t_entity_tipe == "STATION":
        await _ensure_station_exists(record.k_id_station)
    elif record.t_entity_tipe == "LOCK":
        await _ensure_lock_exists(record.k_id_lock)
    payload = record.dict(by_alias=False)
    payload = _normalize_entity_payload(payload, record.t_entity_tipe)
    for key, value in payload.items():
        setattr(db_record, key, value)
    
    db.commit()
    db.refresh(db_record)
    return db_record

@app.delete("/maintenance/{maintenance_id}")
def delete_maintenance_record(maintenance_id: int, db: Session = Depends(database.get_db)):
    db_record = db.query(models.MaintenanceRecord).filter(models.MaintenanceRecord.k_id_maintenance == maintenance_id).first()
    if db_record is None:
        raise HTTPException(status_code=404, detail="Maintenance record not found")
    
    db.delete(db_record)
    db.commit()
    return {"ok": True}
