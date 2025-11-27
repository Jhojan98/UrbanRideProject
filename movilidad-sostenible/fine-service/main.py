import asyncio
import logging
import os
import socket
from datetime import datetime
from typing import Literal, Optional

import sqlalchemy.orm as _orm
from fastapi import Depends, FastAPI, HTTPException
from pydantic import BaseModel
from py_eureka_client import eureka_client as _eureka_client
import httpx

import database as _database
import models as _models

app = FastAPI()
logging.basicConfig(level=logging.INFO)
_eureka_handle = None


# Pydantic models aligned to English DB columns
class FineBase(BaseModel):
    d_descripcion: str
    v_amount: int


class FineCreate(FineBase):
    pass


class FineUpdate(BaseModel):
    d_descripcion: Optional[str] = None
    v_amount: Optional[int] = None


class FineOut(FineBase):
    k_id_fine: int

    class Config:
        orm_mode = True
        from_attributes = True


class UserFineBase(BaseModel):
    n_reason: str
    t_state: Literal['PENDING', 'PAID'] = 'PENDING'
    k_id_fine: int
    k_uid_user: Optional[str] = None


class UserFineCreate(UserFineBase):
    pass


class UserFineUpdate(BaseModel):
    n_reason: Optional[str] = None
    t_state: Optional[Literal['PENDING', 'PAID']] = None
    k_id_fine: Optional[int] = None
    k_uid_user: Optional[str] = None


class UserFineOut(UserFineBase):
    k_user_fine: int
    v_amount_snapshot: Optional[int] = None
    f_assigned_at: datetime
    f_update_at: Optional[datetime] = None
    fine: Optional[FineOut] = None

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


def _to_bool(value: Optional[str], default: bool = True) -> bool:
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "y", "on"}


def _get_users_service_base_url() -> str:
    users_url = os.getenv("USERS_SERVICE_URL", "http://usuario-service:8001")
    users_url = users_url.strip()
    if users_url and not users_url.startswith("http://") and not users_url.startswith("https://"):
        users_url = "http://" + users_url
    return users_url.rstrip("/")


@app.on_event("startup")
async def startup_event():
    global _eureka_handle

    if not _to_bool(os.getenv("EUREKA_ENABLED"), True):
        logging.info("Eureka registration disabled by EUREKA_ENABLED flag")
        return

    eureka_host = os.getenv("EUREKA_HOST", "eureka-server")
    eureka_port = os.getenv("EUREKA_PORT", "8761")
    eureka_server_url = f"http://{eureka_host}:{eureka_port}/eureka/"

    app_name = os.getenv("EUREKA_APP_NAME", "bicycle-service")
    service_port = int(os.getenv("SERVICE_PORT", "5002"))
    service_host = os.getenv("SERVICE_HOST") or socket.gethostname()
    service_ip = os.getenv("SERVICE_IP")
    
    # Configuración de heartbeat (intervalo de renovación)
    heartbeat_interval = int(os.getenv("EUREKA_HEARTBEAT_INTERVAL", "30"))

    register_kwargs = {
        "eureka_server": eureka_server_url,
        "app_name": app_name,
        "instance_host": service_host,
        "instance_port": service_port,
        "renewal_interval_in_secs": heartbeat_interval,
        "duration_in_secs": heartbeat_interval * 3,
        "status_page_url": f"http://{service_host}:{service_port}/",
        "health_check_url": f"http://{service_host}:{service_port}/health",
        "home_page_url": f"http://{service_host}:{service_port}/",
        "metadata": {"framework": "fastapi", "management.port": str(service_port)},
    }

    if service_ip:
        register_kwargs["instance_ip"] = service_ip

    try:
        _eureka_handle = await asyncio.to_thread(_eureka_client.init, **register_kwargs)
        logging.info("Registered FastAPI service '%s' with Eureka at %s", app_name, eureka_server_url)
    except Exception as exc:  # pylint: disable=broad-except
        _eureka_handle = None
        logging.error("Failed to initialize Eureka client: %s", exc)


@app.on_event("shutdown")
async def shutdown_event():
    global _eureka_handle

    if _eureka_handle is None:
        return

    try:
        await asyncio.to_thread(_eureka_handle.stop)
        logging.info("Eureka client stopped")
    except Exception as exc:  # pylint: disable=broad-except
        logging.warning("Error stopping Eureka client: %s", exc)
    finally:
        _eureka_handle = None


@app.get("/")
async def root():
    """Root endpoint"""
    return {"message": "User Fine Service API", "status": "running"}

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy"}

@app.get("/api/fines", response_model=list[FineOut], tags=["Fines"])
async def list_fines(db: _orm.Session = Depends(get_db)):
    """Return all fine catalog entries."""
    fines = db.query(_models.Fine).order_by(_models.Fine.k_id_fine.asc()).all()
    return fines


@app.get("/api/fines/{fine_id}", response_model=FineOut, tags=["Fines"])
async def get_fine(fine_id: int, db: _orm.Session = Depends(get_db)):
    fine = db.query(_models.Fine).filter(_models.Fine.k_id_fine == fine_id).first()
    if not fine:
        raise HTTPException(status_code=404, detail="Fine not found")
    return fine


@app.post("/api/fines", response_model=FineOut, status_code=201, tags=["Fines"])
async def create_fine(data: FineCreate, db: _orm.Session = Depends(get_db)):
    fine = _models.Fine(**data.model_dump())
    try:
        db.add(fine)
        db.commit()
        db.refresh(fine)
        return fine
    except Exception as exc:
        logging.error("Error creating fine: %s", exc)
        db.rollback()
        raise HTTPException(status_code=500, detail="Error creating fine")


@app.put("/api/fines/{fine_id}", response_model=FineOut, tags=["Fines"])
async def update_fine(fine_id: int, data: FineUpdate, db: _orm.Session = Depends(get_db)):
    fine = db.query(_models.Fine).filter(_models.Fine.k_id_fine == fine_id).first()
    if not fine:
        raise HTTPException(status_code=404, detail="Fine not found")

    updated = False
    update_data = data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        if value is None:
            continue
        setattr(fine, field, value)
        updated = True

    if not updated:
        return fine

    try:
        db.commit()
        db.refresh(fine)
        return fine
    except Exception as exc:
        logging.error("Error updating fine %s: %s", fine_id, exc)
        db.rollback()
        raise HTTPException(status_code=500, detail="Error updating fine")


@app.delete("/api/fines/{fine_id}", status_code=204, tags=["Fines"])
async def delete_fine(fine_id: int, db: _orm.Session = Depends(get_db)):
    fine = db.query(_models.Fine).filter(_models.Fine.k_id_fine == fine_id).first()
    if not fine:
        raise HTTPException(status_code=404, detail="Fine not found")

    if fine.user_fines:
        raise HTTPException(status_code=400, detail="Cannot delete fine with assigned user fines")

    try:
        db.delete(fine)
        db.commit()
        return None
    except Exception as exc:
        logging.error("Error deleting fine %s: %s", fine_id, exc)
        db.rollback()
        raise HTTPException(status_code=500, detail="Error deleting fine")


@app.get("/api/user_fines", response_model=list[UserFineOut], tags=["User Fines"])
async def get_user_fines(db: _orm.Session = Depends(get_db)):
    """Get all user fines"""
    try:
        user_fines = db.query(_models.UserFine).options(_orm.joinedload(_models.UserFine.fine)).all()
        return user_fines
    except Exception as e:
        logging.error(f"Error fetching user fines: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/user_fines/{user_fine_id}", response_model=UserFineOut, tags=["User Fines"])
async def get_user_fine(user_fine_id: int, db: _orm.Session = Depends(get_db)):
    """Get a specific user fine by ID"""
    user_fine = (
        db.query(_models.UserFine)
        .options(_orm.joinedload(_models.UserFine.fine))
        .filter(_models.UserFine.k_user_fine == user_fine_id)
        .first()
    )
    if not user_fine:
        raise HTTPException(status_code=404, detail="User Fine not found")
    return user_fine


async def _fetch_user_from_users_service(user_id: str) -> dict:
    base_url = _get_users_service_base_url()
    endpoint = f"{base_url}/login/{user_id}"
    print(f"Calling users service endpoint: {endpoint}")
    
    try:
        print("1. Creating client...")
        async with httpx.AsyncClient(timeout=10.0) as client:
            print("2. Client created, about to GET...")
            resp = await client.get(endpoint)
            print(f"3. GET completed! Status: {resp.status_code}")
            print(f"4. Response body: {resp.text}")
            
            if resp.status_code == 200:
                return resp.json()
            if resp.status_code == 404:
                raise HTTPException(status_code=404, detail="User not found")
            
            logging.error(f"Users service returned {resp.status_code}: {resp.text}")
            raise HTTPException(status_code=502, detail="Users service error")
            
    except httpx.ConnectError as e:
        print(f"ConnectError caught: {e}")
        logging.error("Failed to connect: %s", str(e))
        raise HTTPException(status_code=503, detail="Users service unavailable")
    except httpx.TimeoutException as e:
        print(f"TimeoutException caught: {e}")
        raise HTTPException(status_code=504, detail="Timeout")
    except httpx.HTTPStatusError as e:
        print(f"HTTPStatusError caught: {e}")
        raise HTTPException(status_code=502, detail="HTTP error")
    except Exception as e:
        print(f"Unexpected exception: {type(e).__name__} - {e}")
        logging.error("Unexpected error: %s", str(e))
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")


@app.get("/api/external/users/{user_id}", tags=["External" ])
async def proxy_get_user(user_id: str):
    """Proxy endpoint to fetch a user from the users microservice (for testing)."""
    return await _fetch_user_from_users_service(user_id)


async def _subtract_balance_from_users_service(user_id: str, amount: int) -> dict:
    if amount <= 0:
        raise HTTPException(status_code=400, detail="Amount must be greater than zero")

    base_url = _get_users_service_base_url()
    endpoint = f"{base_url}/balance/{user_id}/subtract"

    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            resp = await client.post(endpoint, params={"amount": amount})
    except httpx.ConnectError as exc:
        logging.error("Failed to connect to users service: %s", exc)
        raise HTTPException(status_code=503, detail="Users service unavailable") from exc
    except httpx.TimeoutException as exc:
        logging.error("Users service request timed out: %s", exc)
        raise HTTPException(status_code=504, detail="Users service timeout") from exc
    except Exception as exc:  # pylint: disable=broad-except
        logging.error("Unexpected error calling users service: %s", exc)
        raise HTTPException(status_code=500, detail="Unexpected error contacting users service") from exc

    if resp.status_code == 200:
        try:
            return resp.json()
        except ValueError:
            logging.error("Users service returned invalid JSON when subtracting balance: %s", resp.text)
            raise HTTPException(status_code=502, detail="Invalid response from users service")

    if resp.status_code == 400:
        try:
            payload = resp.json()
        except ValueError:
            payload = {"error": resp.text or "Saldo insuficiente"}
        message = payload.get("error", "Saldo insuficiente")
        raise HTTPException(status_code=400, detail=message)

    if resp.status_code == 404:
        raise HTTPException(status_code=404, detail="User not found")

    logging.error("Users service subtract balance returned %s: %s", resp.status_code, resp.text)
    raise HTTPException(status_code=502, detail="Users service error")


@app.post("/api/user_fines", response_model=UserFineOut, tags=["User Fines"])
async def create_user_fine(user_fine: UserFineCreate, db: _orm.Session = Depends(get_db)):
    """Create a new user fine"""
    try:
        logging.info(f"Received data: {user_fine.model_dump()}")
        fine = (
            db.query(_models.Fine)
            .filter(_models.Fine.k_id_fine == user_fine.k_id_fine)
            .first()
        )
        if not fine:
            raise HTTPException(status_code=400, detail="Fine not found")

        assigned_at = datetime.utcnow()
        new_user_fine = _models.UserFine(
            n_reason=user_fine.n_reason,
            t_state=user_fine.t_state,
            k_id_fine=user_fine.k_id_fine,
            k_uid_user=user_fine.k_uid_user,
            v_amount_snapshot=fine.v_amount,
            f_assigned_at=assigned_at,
            f_update_at=assigned_at,
        )

        logging.info(f"Created user fine object: {new_user_fine.__dict__}")
        db.add(new_user_fine)
        db.commit()
        db.refresh(new_user_fine)
        _ = new_user_fine.fine
        logging.info(f"User fine created with ID: {new_user_fine.k_user_fine}")
        return new_user_fine
    except Exception as e:
        logging.error(f"Error creating user fine: {e}")
        db.rollback()
        raise HTTPException(status_code=500, detail="Error creating user fine")

@app.get("/api/user_fines/user/{user_id}", response_model=list[UserFineOut], tags=["User Fines"])
async def get_user_fines_by_user(user_id: str, db: _orm.Session = Depends(get_db)):
    """Get all fines for a specific user by their user ID"""
    try:
        user_fines = (
            db.query(_models.UserFine)
            .options(_orm.joinedload(_models.UserFine.fine))
            .filter(_models.UserFine.k_uid_user == user_id)
            .all()
        )
        return user_fines
    except Exception as e:
        logging.error(f"Error fetching user fines for user {user_id}: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")
    
    
@app.post("/api/user_fines/{user_fine_id}/pay", response_model=UserFineOut, tags=["User Fines"])
async def pay_user_fine(user_fine_id: int, db: _orm.Session = Depends(get_db)):
    """Attempt to pay the user fine by subtracting balance from the users service."""
    user_fine = (
        db.query(_models.UserFine)
        .options(_orm.joinedload(_models.UserFine.fine))
        .filter(_models.UserFine.k_user_fine == user_fine_id)
        .first()
    )

    if not user_fine:
        raise HTTPException(status_code=404, detail="User Fine not found")

    if user_fine.t_state == 'PAID':
        raise HTTPException(status_code=400, detail="Fine already paid")

    if not user_fine.k_uid_user:
        raise HTTPException(status_code=400, detail="User identifier not available for this fine")

    amount = user_fine.v_amount_snapshot
    if amount is None and user_fine.fine is not None:
        amount = user_fine.fine.v_amount

    if amount is None:
        raise HTTPException(status_code=400, detail="Fine amount is not available")

    try:
        int_amount = int(amount)
    except (TypeError, ValueError) as exc:
        logging.error("Invalid fine amount %s for user fine %s: %s", amount, user_fine_id, exc)
        raise HTTPException(status_code=400, detail="Fine amount is invalid") from exc

    await _subtract_balance_from_users_service(user_fine.k_uid_user, int_amount)

    user_fine.t_state = 'PAID'
    user_fine.v_amount_snapshot = int_amount
    user_fine.f_update_at = datetime.utcnow()

    try:
        db.commit()
        db.refresh(user_fine)
        _ = user_fine.fine
        logging.info("User Fine %s paid", user_fine_id)
        return user_fine
    except Exception as exc:
        logging.error("Error marking user fine %s as paid: %s", user_fine_id, exc)
        db.rollback()
        raise HTTPException(status_code=500, detail="Error updating user fine")


@app.put("/api/user_fines/{user_fine_id}", response_model=UserFineOut, tags=["User Fines"])
async def update_user_fine(user_fine_id: int, data: UserFineUpdate, db: _orm.Session = Depends(get_db)):
    """Update a user fine"""
    user_fine = (
        db.query(_models.UserFine)
        .options(_orm.joinedload(_models.UserFine.fine))
        .filter(_models.UserFine.k_user_fine == user_fine_id)
        .first()
    )
    if not user_fine:
        raise HTTPException(status_code=404, detail="User Fine not found")

    updated = False

    if data.n_reason is not None:
        user_fine.n_reason = data.n_reason
        updated = True
    if data.t_state is not None:
        user_fine.t_state = data.t_state
        updated = True
    if data.k_id_fine is not None and data.k_id_fine != user_fine.k_id_fine:
        fine = (
            db.query(_models.Fine)
            .filter(_models.Fine.k_id_fine == data.k_id_fine)
            .first()
        )
        if not fine:
            raise HTTPException(status_code=400, detail="Fine not found")

        user_fine.k_id_fine = data.k_id_fine
        user_fine.v_amount_snapshot = fine.v_amount
        updated = True
    if data.k_uid_user is not None:
        user_fine.k_uid_user = data.k_uid_user
        updated = True

    if updated:
        user_fine.f_update_at = datetime.utcnow()

    try:
        db.commit()
        db.refresh(user_fine)
        _ = user_fine.fine
        logging.info("User Fine %s updated", user_fine_id)
        return user_fine
    except Exception as e:
        logging.error("Error updating user fine: %s", e)
        db.rollback()
        raise HTTPException(status_code=500, detail="Error updating user fine")

@app.delete("/api/user_fines/{user_fine_id}", tags=["User Fines"])
async def delete_user_fine(user_fine_id: int, db: _orm.Session = Depends(get_db)):
    """Delete a user fine"""
    user_fine = db.query(_models.UserFine).filter(_models.UserFine.k_user_fine == user_fine_id).first()
    if not user_fine:
        raise HTTPException(status_code=404, detail="User Fine not found")

    try:
        db.delete(user_fine)
        db.commit()
        logging.info(f"User Fine {user_fine_id} deleted")
        return {"message": f"User Fine {user_fine_id} deleted successfully"}
    except Exception as e:
        logging.error(f"Error deleting user fine: {e}")
        db.rollback()
        raise HTTPException(status_code=500, detail="Error deleting user fine")
