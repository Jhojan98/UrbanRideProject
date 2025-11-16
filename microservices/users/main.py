from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
import database as _database
import service as _services
import fastapi as _fastapi
import models as _models
import schemas as _schemas

app = FastAPI(title="Users Service", version="1.0.0")

# Dependency
def get_db():
    db = _database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.get("/health")
async def health():
    return {"status": "ok"}

@app.get("/api/users", response_model=list[_schemas.UserOut])
async def list_users(skip: int = 0, limit: int = 50, db: Session = Depends(get_db)):
    users = await _services.list_users(db, skip=skip, limit=limit)
    return users

@app.post("/api/users", tags=['User Auth'])
async def create_user(user: _schemas.UserCreate, db: Session = _fastapi.Depends(get_db)):
    db_user = await _services.get_user_by_email(db=db, email=user.n_user_email)
    if db_user:
        raise _fastapi.HTTPException(status_code=400, detail="User with that email already exists")
    # Check cedula uniqueness
    existing_cedula = db.query(_models.User).filter(_models.User.k_user_cc == user.k_user_cc).first()
    if existing_cedula:
        raise _fastapi.HTTPException(status_code=400, detail="Cedula already registered")
    created = await _services.create_user(user=user, db=db)
    return {"detail": "User Registered", "k_user_cc": created.k_user_cc}

@app.get("/api/users/{user_id}", response_model=_schemas.UserOut)
async def get_user(user_id: int, db: Session = Depends(get_db)):
    user = await _services.get_user(db, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user

@app.put("/api/users/{user_id}", response_model=_schemas.UserOut)
async def update_user(user_id: int, data: _schemas.UserUpdate, db: Session = Depends(get_db)):
    user = await _services.get_user(db, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    updated = await _services.update_user(db, user, data)
    return updated

@app.delete("/api/users/{user_id}")
async def delete_user(user_id: int, db: Session = Depends(get_db)):
    user = await _services.get_user(db, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    await _services.delete_user(db, user)
    return {"status": "deleted"}

# Public endpoints for auth-service
@app.get("/api/users/public/by-email/{email}", response_model=_schemas.UserBase)
async def public_get_user_by_email(email: str, db: Session = Depends(get_db)):
    user = await _services.get_user_by_email(db=db, email=email)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user

@app.post("/api/users/public/verify", response_model=_schemas.UserBase)
async def public_verify_credentials(identifier: str, password: str, db: Session = Depends(get_db)):
    user = await _services.verify_credentials(db, identifier, password)
    if not user:
        raise HTTPException(status_code=401, detail="Invalid credentials")
    return user

@app.post("/api/users/public/mark-verified", response_model=_schemas.UserBase)
async def public_mark_verified(email: str, db: Session = Depends(get_db)):
    user = await _services.mark_user_verified(db, email)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=5003, reload=True)
