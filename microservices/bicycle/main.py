from typing import List
from fastapi import FastAPI, HTTPException, Depends
import sqlalchemy.orm as _orm
import models as _models
import database as _database
import logging
from pydantic import BaseModel
app = FastAPI()
logging.basicConfig(level=logging.INFO)

# Create tables
_models.Base.metadata.create_all(bind=_database.engine_2)

# Pydantic models
class BicycleCreate(BaseModel):
    serie: str
    modelo: str
    estado: str

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

@app.get("/api/bicycles", tags=["Bicycles"])
async def get_bicycles(db: _orm.Session = Depends(get_db)):
    """Get all bicycles"""
    try:
        bicycles = db.query(_models.Bicycle).all()
        return bicycles
    except Exception as e:
        logging.error(f"Error fetching bicycles: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/bicycles/{bicycle_id}", tags=["Bicycles"])
async def get_bicycle(bicycle_id: int, db: _orm.Session = Depends(get_db)):
    """Get a specific bicycle by ID"""
    bicycle = db.query(_models.Bicycle).filter(_models.Bicycle.idBicileta == bicycle_id).first()
    if not bicycle:
        raise HTTPException(status_code=404, detail="Bicycle not found")
    return bicycle

@app.post("/api/bicycles", tags=["Bicycles"])
async def create_bicycle(
    bicycle: BicycleCreate,
    db: _orm.Session = Depends(get_db)
):
    """Create a new bicycle"""
    try:
        new_bicycle = _models.Bicycle(
            serie=bicycle.serie,
            modelo=bicycle.modelo,
            estado=bicycle.estado
        )
        db.add(new_bicycle)
        db.commit()
        db.refresh(new_bicycle)
        logging.info(f"Bicycle created with ID: {new_bicycle.idBicileta}")
        return new_bicycle
    except Exception as e:
        logging.error(f"Error creating bicycle: {e}")
        db.rollback()
        raise HTTPException(status_code=500, detail="Error creating bicycle")

@app.put("/api/bicycles/{bicycle_id}", tags=["Bicycles"])
async def update_bicycle(
    bicycle_id: int,
    serie: str = None,
    modelo: str = None,
    estado: str = None,
    db: _orm.Session = Depends(get_db)
):
    """Update a bicycle"""
    bicycle = db.query(_models.Bicycle).filter(_models.Bicycle.idBicileta == bicycle_id).first()
    if not bicycle:
        raise HTTPException(status_code=404, detail="Bicycle not found")
    
    if serie is not None:
        bicycle.serie = serie
    if modelo is not None:
        bicycle.modelo = modelo
    if estado is not None:
        bicycle.estado = estado
    
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
    bicycle = db.query(_models.Bicycle).filter(_models.Bicycle.idBicileta == bicycle_id).first()
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
