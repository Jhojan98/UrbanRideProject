from typing import List, Optional, Literal
from fastapi import FastAPI, HTTPException, Depends
import sqlalchemy.orm as _orm
import models as _models
import database as _database
import logging
from pydantic import BaseModel, Field
from datetime import datetime

app = FastAPI()
logging.basicConfig(level=logging.INFO)


# Pydantic models aligned to English DB columns
class BicycleBase(BaseModel):
    k_series: int
    n_model: str
    t_padlock_status: Literal['CLOSE', 'OPEN'] = 'CLOSE'
    f_last_update: Optional[datetime]
    n_latitude: Optional[float]
    n_length: Optional[float]
    v_battery: Optional[float] = Field(default=None, ge=0, le=100)

class BicycleCreate(BicycleBase):
    pass

class BicycleUpdate(BaseModel):
    n_model: Optional[str] = None
    t_padlock_status: Optional[Literal['CLOSE', 'OPEN']] = None
    f_last_update: Optional[datetime] = None
    n_latitude: Optional[float] = None
    n_length: Optional[float] = None
    v_battery: Optional[float] = Field(default=None, ge=0, le=100)

class BicycleOut(BicycleBase):
    k_id_bicycle: int
    class Config:
        orm_mode = True
        from_attributes = True

# Dependency to get DB session
def get_db():
    db = _database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.get("/")
async def root():
    """Root endpoint"""
    return {"message": "Bicycle Service API", "status": "running"}

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy"}

@app.get("/api/bicycles", response_model=list[BicycleOut], tags=["Bicycles"])
async def get_bicycles(db: _orm.Session = Depends(get_db)):
    """Get all bicycles"""
    try:
        bicycles = db.query(_models.Bicycle).all()
        return bicycles
    except Exception as e:
        logging.error(f"Error fetching bicycles: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/bicycles/{bicycle_id}", response_model=BicycleOut, tags=["Bicycles"])
async def get_bicycle(bicycle_id: int, db: _orm.Session = Depends(get_db)):
    """Get a specific bicycle by ID"""
    bicycle = db.query(_models.Bicycle).filter(_models.Bicycle.k_id_bicycle == bicycle_id).first()
    if not bicycle:
        raise HTTPException(status_code=404, detail="Bicycle not found")
    return bicycle

@app.post("/api/bicycles", response_model=BicycleOut, tags=["Bicycles"])
async def create_bicycle(bicycle: BicycleCreate, db: _orm.Session = Depends(get_db)):
    """Create a new bicycle"""
    try:
        logging.info(f"Received data: {bicycle.model_dump()}")
        new_bicycle = _models.Bicycle(
            k_series=bicycle.k_series,
            n_model=bicycle.n_model,
            t_padlock_status=bicycle.t_padlock_status,
            f_last_update=bicycle.f_last_update,
            n_latitude=bicycle.n_latitude,
            n_length=bicycle.n_length,
            v_battery=bicycle.v_battery,
        )

        logging.info(f"Created bicycle object: {new_bicycle.__dict__}")
        db.add(new_bicycle)
        db.commit()
        db.refresh(new_bicycle)
        logging.info(f"Bicycle created with ID: {new_bicycle.k_id_bicycle}")
        return new_bicycle
    except Exception as e:
        logging.error(f"Error creating bicycle: {e}")
        db.rollback()
        raise HTTPException(status_code=500, detail="Error creating bicycle")

@app.put("/api/bicycles/{bicycle_id}", response_model=BicycleOut, tags=["Bicycles"])
async def update_bicycle(bicycle_id: int, data: BicycleUpdate, db: _orm.Session = Depends(get_db)):
    """Update a bicycle"""
    bicycle = db.query(_models.Bicycle).filter(_models.Bicycle.k_id_bicycle == bicycle_id).first()
    if not bicycle:
        raise HTTPException(status_code=404, detail="Bicycle not found")

    if data.n_model is not None:
        bicycle.n_model = data.n_model
    if data.t_padlock_status is not None:
        bicycle.t_padlock_status = data.t_padlock_status
    if data.f_last_update is not None:
        bicycle.f_last_update = data.f_last_update
    if data.n_latitude is not None:
        bicycle.n_latitude = data.n_latitude
    if data.n_length is not None:
        bicycle.n_length = data.n_length
    if data.v_battery is not None:
        bicycle.v_battery = data.v_battery

    try:
        db.commit()
        db.refresh(bicycle)
        logging.info(f"Bicycle {bicycle_id} updated")
        return bicycle
    except Exception as e:
        logging.error(f"Error updating bicycle: {e}")
        db.rollback()
        raise HTTPException(status_code=500, detail="Error updating bicycle")

@app.delete("/api/bicycles/{bicycle_id}", tags=["Bicycles"])
async def delete_bicycle(bicycle_id: int, db: _orm.Session = Depends(get_db)):
    """Delete a bicycle"""
    bicycle = db.query(_models.Bicycle).filter(_models.Bicycle.k_id_bicycle == bicycle_id).first()
    if not bicycle:
        raise HTTPException(status_code=404, detail="Bicycle not found")

    try:
        db.delete(bicycle)
        db.commit()
        logging.info(f"Bicycle {bicycle_id} deleted")
        return {"message": f"Bicycle {bicycle_id} deleted successfully"}
    except Exception as e:
        logging.error(f"Error deleting bicycle: {e}")
        db.rollback()
        raise HTTPException(status_code=500, detail="Error deleting bicycle")
