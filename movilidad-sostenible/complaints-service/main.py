from eureka import start as start_eureka, stop as stop_eureka
import os
from contextlib import asynccontextmanager
from fastapi import Depends, FastAPI, HTTPException
import logging
import models
import schemas
import database
import httpx
from sqlalchemy.orm import Session
@asynccontextmanager
async def lifespan(_: FastAPI):
    eureka_handle = await start_eureka()
    try:
        yield
    finally:
        await stop_eureka(eureka_handle)

app = FastAPI(lifespan=lifespan, title="Complaints Service")
logging.basicConfig(level=logging.INFO)

async def _ensure_travel_exists(travel_id: int, db: Session) -> None:
    """Helper to verify that a travel exists in the Travel service."""
    travel_service_url = os.getenv("TRAVEL_SERVICE_URL", "http://viaje-service:8003")
    url = f"{travel_service_url}/{travel_id}"
    async with httpx.AsyncClient() as client:
        response = await client.get(url)
        if response.status_code == 404:
            raise HTTPException(status_code=400, detail=f"Travel with ID {travel_id} does not exist.")
        response.raise_for_status()

@app.get("/")
async def root():
    """Root endpoint providing basic service info."""
    return {"message": "Complaints Service API", "status": "running"}

@app.get("/health")
async def health_check():
    """Health check endpoint."""
    return {"status": "healthy"}

@app.get("/complaints/", response_model=list[schemas.ComplaintOut])
async def read_complaints(skip: int = 0, limit: int = 10, db=Depends(database.get_db)):
    """Retrieve a list of complaints."""
    records = db.query(models.Complaint).offset(skip).limit(limit).all()
    return records

@app.post("/complaints/", response_model=schemas.ComplaintOut)
async def create_complaint(complaint: schemas.ComplaintCreate, db=Depends(database.get_db)):
    """Create a new complaint."""
    if complaint.k_id_travel is not None:
        await _ensure_travel_exists(complaint.k_id_travel, db)
    db_complaint = models.Complaint(**complaint.dict())
    db.add(db_complaint)
    db.commit()
    db.refresh(db_complaint)
    return db_complaint

@app.get("/complaints/{complaint_id}", response_model=schemas.ComplaintOut)
async def read_complaint(complaint_id: int, db=Depends(database.get_db)):
    """Retrieve a specific complaint by ID."""
    record = db.query(models.Complaint).filter(models.Complaint.k_id_complaints_and_claims == complaint_id).first()
    if record is None:
        raise HTTPException(status_code=404, detail="Complaint not found")
    return record
@app.put("/complaints/{complaint_id}", response_model=schemas.ComplaintOut)
async def update_complaint(complaint_id: int, complaint: schemas.ComplaintUpdate, db=Depends(database.get_db)):
    """Update an existing complaint."""
    db_complaint = db.query(models.Complaint).filter(models.Complaint.k_id_complaints_and_claims == complaint_id).first()
    if db_complaint is None:
        raise HTTPException(status_code=404, detail="Complaint not found")
    
    update_data = complaint.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_complaint, key, value)
    
    db.commit()
    db.refresh(db_complaint)
    return db_complaint