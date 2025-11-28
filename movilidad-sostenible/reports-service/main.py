import asyncio
import logging
import os
import socket
from typing import Optional

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from py_eureka_client import eureka_client as _eureka_client
import httpx
from fastapi.responses import StreamingResponse

from aggregator import ReportAggregator
from excel import build_admin_overview_excel, build_user_dashboard_excel

app = FastAPI()
logging.basicConfig(level=logging.INFO)
_eureka_handle = None


# Pydantic models aligned to English DB columns
class FineBase(BaseModel):
    d_descripcion: str
    v_amount: int




def _to_bool(value: Optional[str], default: bool = True) -> bool:
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "y", "on"}


def _get_users_service_base_url() -> str:
    users_url = os.getenv("USERS_SERVICE_URL", "http://usuario-service:8001")
    print(f"Users service URL from env: {users_url}")
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

    app_name = os.getenv("EUREKA_APP_NAME", "reports-service")
    service_port = int(os.getenv("SERVICE_PORT", "5004"))
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
    return {"message": "Reports Service API", "status": "running"}

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy"}

@app.get("/api/reports", response_model=dict, tags=["Reports"])
async def get_reports():
    """Get basic report data"""
    try:
        # Aquí se podrían agregar consultas a la base de datos para obtener datos reales
        report_data = {
            "total_fines": 150,
            "total_user_fines": 75,
            "total_paid_fines": 50,
            "total_pending_fines": 25,
        }
        return report_data
    except Exception as e:
        logging.error(f"Error fetching report data: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")
    

    


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


# Deprecated DB-based endpoint removed; fines are available via aggregator endpoints.


# Aggregator Pattern endpoints
@app.get("/api/admin/overview", tags=["Reports", "Admin"])
async def admin_overview():
    aggregator = ReportAggregator()
    try:
        overview = await aggregator.aggregate_overview()
        return overview
    except httpx.HTTPError as e:
        logging.error("Error aggregating admin overview: %s", e)
        raise HTTPException(status_code=502, detail="Dependency error")
    except Exception as e:
        logging.error("Unexpected error in admin overview: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")


@app.get("/api/admin/overview.xlsx", tags=["Reports", "Admin"])
async def admin_overview_excel():
    aggregator = ReportAggregator()
    try:
        overview = await aggregator.aggregate_overview()
        content = build_admin_overview_excel(overview)
        return StreamingResponse(
            iter([content]),
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            headers={
                "Content-Disposition": "attachment; filename=admin-overview.xlsx",
            },
        )
    except httpx.HTTPError as e:
        logging.error("Error aggregating admin overview excel: %s", e)
        raise HTTPException(status_code=502, detail="Dependency error")
    except Exception as e:
        logging.error("Unexpected error generating excel: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")


@app.get("/api/users/{user_id}/dashboard", tags=["Reports", "Users"])
async def user_dashboard(user_id: str):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_user_dashboard(user_id)
        return data
    except httpx.HTTPError as e:
        logging.error("Error aggregating user dashboard: %s", e)
        raise HTTPException(status_code=502, detail="Dependency error")
    except HTTPException:
        raise
    except Exception as e:
        logging.error("Unexpected error in user dashboard: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")


@app.get("/api/users/{user_id}/dashboard.xlsx", tags=["Reports", "Users"])
async def user_dashboard_excel(user_id: str):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_user_dashboard(user_id)
        content = build_user_dashboard_excel(data)
        return StreamingResponse(
            iter([content]),
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            headers={
                "Content-Disposition": f"attachment; filename=user-{user_id}-dashboard.xlsx",
            },
        )
    except httpx.HTTPError as e:
        logging.error("Error aggregating user dashboard excel: %s", e)
        raise HTTPException(status_code=502, detail="Dependency error")
    except HTTPException:
        raise
    except Exception as e:
        logging.error("Unexpected error generating excel: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")


# --- New Reports Endpoints ---

@app.get("/api/reports/bicycle-usage", tags=["Reports"])
async def report_bicycle_usage():
    """Frecuencia de uso de bicicletas."""
    aggregator = ReportAggregator()
    try:
        return await aggregator.aggregate_bicycle_usage()
    except Exception as e:
        logging.error("Error in bicycle usage report: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/reports/station-demand", tags=["Reports"])
async def report_station_demand():
    """Estaciones de mayor demanda."""
    aggregator = ReportAggregator()
    try:
        return await aggregator.aggregate_station_demand()
    except Exception as e:
        logging.error("Error in station demand report: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/reports/service-demand", tags=["Reports"])
async def report_service_demand():
    """Servicio con mayor demanda (última milla vs recorrido largo)."""
    aggregator = ReportAggregator()
    try:
        return await aggregator.aggregate_service_demand()
    except Exception as e:
        logging.error("Error in service demand report: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/reports/bicycle-demand", tags=["Reports"])
async def report_bicycle_demand():
    """Bicicleta de mayor demanda (Eléctrica o Mecánica)."""
    aggregator = ReportAggregator()
    try:
        return await aggregator.aggregate_bicycle_demand_by_type()
    except Exception as e:
        logging.error("Error in bicycle demand report: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/reports/daily-trips", tags=["Reports"])
async def report_daily_trips():
    """Viajes por dia."""
    aggregator = ReportAggregator()
    try:
        return await aggregator.aggregate_daily_trips()
    except Exception as e:
        logging.error("Error in daily trips report: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/reports/maintenances", tags=["Reports"])
async def report_maintenances():
    """Mantenimientos."""
    aggregator = ReportAggregator()
    try:
        return await aggregator.aggregate_maintenances()
    except Exception as e:
        logging.error("Error in maintenances report: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")
