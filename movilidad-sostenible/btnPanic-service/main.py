from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
import contextlib
import logging

import crud, models, schemas, database
from eureka import start as start_eureka, stop as stop_eureka
from rabbit import start_panic_consumer, stop_panic_consumer

# Configure logging
logging.basicConfig(level=logging.INFO)

# Create tables
models.Base.metadata.create_all(bind=database.engine_2)

@contextlib.asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup logic
    eureka_handle = await start_eureka()
    start_panic_consumer()
    yield
    # Shutdown logic
    stop_panic_consumer()
    await stop_eureka(eureka_handle)

app = FastAPI(lifespan=lifespan)

# Dependency
def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.post("/panic_button/", response_model=schemas.panic_buttonOut)
def create_panic_button(panic_button: schemas.panic_buttonCreate, db: Session = Depends(get_db)):
    return crud.create_panic_button(db=db, panic_button=panic_button)

@app.get("/panic_button/", response_model=List[schemas.panic_buttonOut])
def read_panic_buttons(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    panic_buttons = crud.get_panic_buttons(db, skip=skip, limit=limit)
    return panic_buttons

@app.get("/panic_button/{panic_button_id}", response_model=schemas.panic_buttonOut)
def read_panic_button(panic_button_id: int, db: Session = Depends(get_db)):
    db_panic_button = crud.get_panic_button(db, panic_button_id=panic_button_id)
    if db_panic_button is None:
        raise HTTPException(status_code=404, detail="Panic button activation not found")
    return db_panic_button
