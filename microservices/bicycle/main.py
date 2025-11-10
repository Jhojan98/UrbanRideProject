from typing import List, Optional
from fastapi import FastAPI, HTTPException, Depends
import sqlalchemy.orm as _orm
import models as _models
import database as _database
import logging
from pydantic import BaseModel, Field
from datetime import datetime

app = FastAPI()
logging.basicConfig(level=logging.INFO)


# Pydantic models aligned to DB columns
class BicycleBase(BaseModel):
    k_serie: int 
    n_modelo: str 
    t_estado_candado: str 
    f_ultima_actualizacion: Optional[datetime] 
    n_latitud: Optional[float] 
    n_longitud: Optional[float] 
    v_bateria: Optional[float] 

class BicycleCreate(BicycleBase):
    pass

class BicycleUpdate(BaseModel):
    n_modelo: Optional[str] 
    t_estado_candado: Optional[str] 
    f_ultima_actualizacion: Optional[datetime] 
    n_latitud: Optional[float] 
    n_longitud: Optional[float] 
    v_bateria: Optional[float] 

class BicycleOut(BicycleBase):
    k_id_bicicleta: int
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
    bicycle = db.query(_models.Bicycle).filter(_models.Bicycle.k_id_bicicleta == bicycle_id).first()
    if not bicycle:
        raise HTTPException(status_code=404, detail="Bicycle not found")
    return bicycle

@app.post("/api/bicycles", response_model=BicycleOut, tags=["Bicycles"])
async def create_bicycle(bicycle: BicycleCreate, db: _orm.Session = Depends(get_db)):
    """Create a new bicycle"""
    try:
        logging.info(f"Received data: {bicycle.model_dump()}")
        new_bicycle = _models.Bicycle(
            k_serie=bicycle.k_serie,
            n_modelo=bicycle.n_modelo,
            t_estado_candado=bicycle.t_estado_candado,
            f_ultima_actualizacion=bicycle.f_ultima_actualizacion,
            n_latitud=bicycle.n_latitud,
            n_longitud=bicycle.n_longitud,
            v_bateria=bicycle.v_bateria,
        )

        logging.info(f"Created bicycle object: {new_bicycle.__dict__}")
        db.add(new_bicycle)
        db.commit()
        db.refresh(new_bicycle)
        logging.info(f"Bicycle created with ID: {new_bicycle.k_id_bicicleta}")
        return new_bicycle
    except Exception as e:
        logging.error(f"Error creating bicycle: {e}")
        db.rollback()
        raise HTTPException(status_code=500, detail="Error creating bicycle")

@app.put("/api/bicycles/{bicycle_id}", response_model=BicycleOut, tags=["Bicycles"])
async def update_bicycle(bicycle_id: int, data: BicycleUpdate, db: _orm.Session = Depends(get_db)):
    """Update a bicycle"""
    bicycle = db.query(_models.Bicycle).filter(_models.Bicycle.k_id_bicicleta == bicycle_id).first()
    if not bicycle:
        raise HTTPException(status_code=404, detail="Bicycle not found")

    if data.n_modelo is not None:
        bicycle.n_modelo = data.n_modelo
    if data.t_estado_candado is not None:
        bicycle.t_estado_candado = data.t_estado_candado
    if data.f_ultima_actualizacion is not None:
        bicycle.f_ultima_actualizacion = data.f_ultima_actualizacion
    if data.n_latitud is not None:
        bicycle.n_latitud = data.n_latitud
    if data.n_longitud is not None:
        bicycle.n_longitud = data.n_longitud
    if data.v_bateria is not None:
        bicycle.v_bateria = data.v_bateria

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
    bicycle = db.query(_models.Bicycle).filter(_models.Bicycle.k_id_bicicleta == bicycle_id).first()
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
