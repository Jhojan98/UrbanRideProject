from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy import text
from sqlalchemy.orm import Session
from typing import List, Optional
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

async def _ensure_bicycle_exists(bike_id: str) -> dict:
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

@app.post("/maintenance/", response_model=schemas.MaintenanceRecord)
async def create_maintenance_record(record: schemas.MaintenanceRecordCreate, db: Session = Depends(database.get_db)):
    payload = record.dict(by_alias=False)
    await _ensure_bicycle_exists(payload.get("k_id_bicycle"))
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
    
    if record.k_id_bicycle:
        await _ensure_bicycle_exists(record.k_id_bicycle)
    payload = record.dict(by_alias=False)
    await _ensure_bicycle_exists(payload.get("k_id_bicycle"))

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
