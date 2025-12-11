import logging
import os
import uuid
from contextlib import asynccontextmanager
from typing import List

import httpx
from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from py_eureka_client import eureka_client

import database
import models
import schemas
from eureka import start as start_eureka, stop as stop_eureka
from rabbit import EXCHANGE, close_rabbit_channel, open_rabbit_channel, start_iot_consumer, stop_iot_consumer



@asynccontextmanager
async def lifespan(app: FastAPI):
    eureka_handle = await start_eureka()
    rabbit_connection = None
    rabbit_channel = None
    consumer = None
    try:
        rabbit_connection, rabbit_channel = open_rabbit_channel()
        app.state.rabbit_connection = rabbit_connection
        app.state.rabbit_channel = rabbit_channel
        logging.info("RabbitMQ channel ready on exchange '%s'", EXCHANGE)
        consumer = start_iot_consumer()
        yield
    finally:
        stop_iot_consumer()
        close_rabbit_channel(rabbit_connection, rabbit_channel)
        await stop_eureka(eureka_handle)


logging.basicConfig(level=logging.INFO)
models.Base.metadata.create_all(bind=database.engine)

app = FastAPI(lifespan=lifespan, title="Mantenimiento Service")


async def _resolve_service_url(service_name: str) -> str:
    eureka_host = os.getenv("EUREKA_HOST", "eureka-server")
    eureka_port = os.getenv("EUREKA_PORT", "8761")
    eureka_server_url = f"http://{eureka_host}:{eureka_port}/eureka/"

    try:
        app = await eureka_client.get_application(eureka_server_url, service_name)
        for instance in app.instances:
            if instance.status == "UP":
                # Prefer IP+port to avoid hostnames that are not resolvable from inside the container
                return f"http://{instance.ipAddr}:{instance.port.port}"

        logging.error(f"No 'UP' instances found for service: {service_name}")
        raise HTTPException(status_code=503, detail=f"Service unavailable: {service_name}")
    except Exception as e:
        logging.error(f"Failed to resolve service '{service_name}': {e}")
        raise HTTPException(status_code=503, detail=f"Service discovery failed for {service_name}")


async def _ensure_bicycle_exists(bike_id: str) -> dict:
    if not bike_id:
        raise HTTPException(status_code=400, detail="Bike ID is required for bicycle maintenance records.")

    base_url = await _resolve_service_url("bicis-service")
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

    base_url = await _resolve_service_url("estaciones-service")
    endpoint = f"{base_url}/{station_id}"
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

    # Assuming 'slots-service' is the correct name for locks
    base_url = await _resolve_service_url("slots-service")
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
    entity = record.t_entity_type
    target_field = {
        "BICYCLE": record.k_id_bicycle,
        "STATION": record.k_id_station,
        "LOCK": record.k_id_slot,
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
        payload["k_id_slot"] = None
    return payload

@app.post("/maintenance/", response_model=schemas.MaintenanceRecord)
async def create_maintenance_record(record: schemas.MaintenanceRecordCreate, db: Session = Depends(database.get_db)):
    _validate_entity_reference(record)
    if record.t_entity_type == "BICYCLE":
        await _ensure_bicycle_exists(record.k_id_bicycle)
    elif record.t_entity_type == "STATION":
        await _ensure_station_exists(record.k_id_station)
    elif record.t_entity_type == "LOCK":
        await _ensure_lock_exists(record.k_id_slot)
    payload = record.dict(by_alias=False)
    payload = _normalize_entity_payload(payload, record.t_entity_type)
    if not payload.get("k_id_maintenance"):
        payload["k_id_maintenance"] = str(uuid.uuid4())
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
def read_maintenance_record(maintenance_id: str, db: Session = Depends(database.get_db)):
    record = db.query(models.MaintenanceRecord).filter(models.MaintenanceRecord.k_id_maintenance == maintenance_id).first()
    if record is None:
        raise HTTPException(status_code=404, detail="Maintenance record not found")
    return record

@app.put("/maintenance/{maintenance_id}", response_model=schemas.MaintenanceRecord)
async def update_maintenance_record(maintenance_id: str, record: schemas.MaintenanceRecordCreate, db: Session = Depends(database.get_db)):
    db_record = db.query(models.MaintenanceRecord).filter(models.MaintenanceRecord.k_id_maintenance == maintenance_id).first()
    if db_record is None:
        raise HTTPException(status_code=404, detail="Maintenance record not found")

    _validate_entity_reference(record)
    if record.t_entity_type == "BICYCLE":
        await _ensure_bicycle_exists(record.k_id_bicycle)
    elif record.t_entity_type == "STATION":
        await _ensure_station_exists(record.k_id_station)
    elif record.t_entity_type == "LOCK":
        await _ensure_lock_exists(record.k_id_slot)
    payload = record.dict(by_alias=False)
    payload = _normalize_entity_payload(payload, record.t_entity_type)
    for key, value in payload.items():
        setattr(db_record, key, value)

    db.commit()
    db.refresh(db_record)
    return db_record


@app.patch("/maintenance/{maintenance_id}", response_model=schemas.MaintenanceRecord)
async def patch_maintenance_record(maintenance_id: str, record: schemas.MaintenancePartialUpdate, db: Session = Depends(database.get_db)):
    db_record = db.query(models.MaintenanceRecord).filter(models.MaintenanceRecord.k_id_maintenance == maintenance_id).first()
    if db_record is None:
        raise HTTPException(status_code=404, detail="Maintenance record not found")

    updates = record.dict(exclude_unset=True, by_alias=False)
    final_entity = updates.get("t_entity_type", db_record.t_entity_type)
    new_bike = updates.get("k_id_bicycle", db_record.k_id_bicycle)
    new_station = updates.get("k_id_station", db_record.k_id_station)
    new_slot = updates.get("k_id_slot", db_record.k_id_slot)

    # Validate entity/id coherence
    if final_entity == "BICYCLE" and not new_bike:
        raise HTTPException(status_code=400, detail="Bicycle maintenance requires bikeId")
    if final_entity == "STATION" and new_station is None:
        raise HTTPException(status_code=400, detail="Station maintenance requires stationId")
    if final_entity == "LOCK" and not new_slot:
        raise HTTPException(status_code=400, detail="Lock maintenance requires lockId")

    # Cross-service existence checks when ids change
    if "k_id_bicycle" in updates or "t_entity_type" in updates:
        if final_entity == "BICYCLE":
            await _ensure_bicycle_exists(new_bike)
    if "k_id_station" in updates or "t_entity_type" in updates:
        if final_entity == "STATION":
            await _ensure_station_exists(new_station)
    if "k_id_slot" in updates or "t_entity_type" in updates:
        if final_entity == "LOCK":
            await _ensure_lock_exists(new_slot)

    # Normalize ids to match final entity type
    updates["k_id_bicycle"] = new_bike if final_entity == "BICYCLE" else None
    updates["k_id_station"] = new_station if final_entity == "STATION" else None
    updates["k_id_slot"] = new_slot if final_entity == "LOCK" else None
    updates["t_entity_type"] = final_entity

    for key, value in updates.items():
        setattr(db_record, key, value)

    db.commit()
    db.refresh(db_record)
    return db_record


@app.patch("/maintenance/{maintenance_id}/status", response_model=schemas.MaintenanceRecord)
def update_maintenance_status(maintenance_id: str, body: schemas.MaintenanceStatusUpdate, db: Session = Depends(database.get_db)):
    db_record = db.query(models.MaintenanceRecord).filter(models.MaintenanceRecord.k_id_maintenance == maintenance_id).first()
    if db_record is None:
        raise HTTPException(status_code=404, detail="Maintenance record not found")

    db_record.t_status = body.t_status
    db.commit()
    db.refresh(db_record)
    return db_record

@app.delete("/maintenance/{maintenance_id}")
def delete_maintenance_record(maintenance_id: str, db: Session = Depends(database.get_db)):
    db_record = db.query(models.MaintenanceRecord).filter(models.MaintenanceRecord.k_id_maintenance == maintenance_id).first()
    if db_record is None:
        raise HTTPException(status_code=404, detail="Maintenance record not found")

    db.delete(db_record)
    db.commit()
    return {"ok": True}
