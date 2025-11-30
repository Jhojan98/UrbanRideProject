from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

import database
import models
import schemas

models.Base.metadata.create_all(bind=database.engine)

app = FastAPI(title="Mantenimiento Service")

@app.post("/maintenance/", response_model=schemas.MaintenanceRecord)
def create_maintenance_record(record: schemas.MaintenanceRecordCreate, db: Session = Depends(database.get_db)):
    db_record = models.MaintenanceRecord(**record.dict(by_alias=False))
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
def update_maintenance_record(maintenance_id: int, record: schemas.MaintenanceRecordCreate, db: Session = Depends(database.get_db)):
    db_record = db.query(models.MaintenanceRecord).filter(models.MaintenanceRecord.k_id_maintenance == maintenance_id).first()
    if db_record is None:
        raise HTTPException(status_code=404, detail="Maintenance record not found")
    
    for key, value in record.dict(by_alias=False).items():
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
