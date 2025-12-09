from sqlalchemy.orm import Session
import logging
import datetime
import models, schemas

def get_panic_button(db: Session, panic_button_id: int):
    return db.query(models.panic_button).filter(models.panic_button.k_id_panic_button == panic_button_id).first()

def get_panic_buttons(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.panic_button).offset(skip).limit(limit).all()

def create_panic_button(db: Session, panic_button: schemas.panic_buttonCreate):
    db_panic_button = models.panic_button(
        k_id_station=panic_button.k_id_station,
        f_activation_date=panic_button.f_activation_date or datetime.datetime.now()
    )
    db.add(db_panic_button)
    db.commit()
    db.refresh(db_panic_button)
    return db_panic_button
