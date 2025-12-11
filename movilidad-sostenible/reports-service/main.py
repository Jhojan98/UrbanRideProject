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
from report_exports import (
    build_admin_overview_excel_pandas,
    build_admin_overview_pdf,
    build_user_dashboard_excel_pandas,
    build_user_dashboard_pdf,
)

app = FastAPI()
logging.basicConfig(level=logging.INFO)
_eureka_handle = None



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

 












@app.get("/api/admin/overview.xlsx", tags=["Reports", "Admin"])
async def admin_overview_excel(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        overview = await aggregator.aggregate_overview(lang=lang)
        content = build_admin_overview_excel_pandas(overview)
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


@app.get("/api/admin/overview.pdf", tags=["Reports", "Admin"])
async def admin_overview_pdf(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        overview = await aggregator.aggregate_overview(lang=lang)
        content = build_admin_overview_pdf(overview)
        return StreamingResponse(
            iter([content]),
            media_type="application/pdf",
            headers={"Content-Disposition": "attachment; filename=admin-overview.pdf"},
        )
    except httpx.HTTPError as e:
        logging.error("Error aggregating admin overview pdf: %s", e)
        raise HTTPException(status_code=502, detail="Dependency error")
    except Exception as e:
        logging.error("Unexpected error generating pdf: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")








@app.get("/api/users/{user_id}/dashboard.xlsx", tags=["Reports", "Users"])
async def user_dashboard_excel(user_id: str, lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_user_dashboard(user_id, lang=lang)
        content = build_user_dashboard_excel_pandas(data)
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


@app.get("/api/users/{user_id}/dashboard.pdf", tags=["Reports", "Users"])
async def user_dashboard_pdf(user_id: str, lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_user_dashboard(user_id, lang=lang)
        content = build_user_dashboard_pdf(data)
        return StreamingResponse(
            iter([content]),
            media_type="application/pdf",
            headers={"Content-Disposition": f"attachment; filename=user-{user_id}-dashboard.pdf"},
        )
    except httpx.HTTPError as e:
        logging.error("Error aggregating user dashboard pdf: %s", e)
        raise HTTPException(status_code=502, detail="Dependency error")
    except HTTPException:
        raise
    except Exception as e:
        logging.error("Unexpected error generating pdf: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")






@app.get("/api/reports/bicycle-usage.xlsx", tags=["Reports"])
async def report_bicycle_usage_excel(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_bicycle_usage()
        from report_exports import build_single_report_excel
        from graphs import generate_bicycle_usage_chart
        content = build_single_report_excel(data, "bicycle_usage", lang, generate_bicycle_usage_chart)
        return StreamingResponse(
            iter([content]),
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            headers={"Content-Disposition": "attachment; filename=bicycle-usage.xlsx"},
        )
    except Exception as e:
        logging.error("Error generating bicycle usage excel: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/reports/bicycle-usage.pdf", tags=["Reports"])
async def report_bicycle_usage_pdf(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_bicycle_usage()
        from report_exports import build_single_report_pdf
        from graphs import generate_bicycle_usage_chart
        content = build_single_report_pdf(data, "bicycle_usage", lang, generate_bicycle_usage_chart)
        return StreamingResponse(
            iter([content]),
            media_type="application/pdf",
            headers={"Content-Disposition": "attachment; filename=bicycle-usage.pdf"},
        )
    except Exception as e:
        logging.error("Error generating bicycle usage pdf: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")




@app.get("/api/reports/station-demand.xlsx", tags=["Reports"])
async def report_station_demand_excel(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_station_demand()
        from report_exports import build_single_report_excel
        from graphs import generate_station_demand_chart
        content = build_single_report_excel(data, "station_demand", lang, generate_station_demand_chart)
        return StreamingResponse(
            iter([content]),
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            headers={"Content-Disposition": "attachment; filename=station-demand.xlsx"},
        )
    except Exception as e:
        logging.error("Error generating station demand excel: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/reports/station-demand.pdf", tags=["Reports"])
async def report_station_demand_pdf(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_station_demand()
        from report_exports import build_single_report_pdf
        from graphs import generate_station_demand_chart
        content = build_single_report_pdf(data, "station_demand", lang, generate_station_demand_chart)
        return StreamingResponse(
            iter([content]),
            media_type="application/pdf",
            headers={"Content-Disposition": "attachment; filename=station-demand.pdf"},
        )
    except Exception as e:
        logging.error("Error generating station demand pdf: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")




@app.get("/api/reports/service-demand.xlsx", tags=["Reports"])
async def report_service_demand_excel(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_service_demand()
        from report_exports import build_single_report_excel
        from graphs import generate_service_demand_chart
        content = build_single_report_excel(data, "service_demand", lang, generate_service_demand_chart)
        return StreamingResponse(
            iter([content]),
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            headers={"Content-Disposition": "attachment; filename=service-demand.xlsx"},
        )
    except Exception as e:
        logging.error("Error generating service demand excel: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/reports/service-demand.pdf", tags=["Reports"])
async def report_service_demand_pdf(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_service_demand()
        from report_exports import build_single_report_pdf
        from graphs import generate_service_demand_chart
        content = build_single_report_pdf(data, "service_demand", lang, generate_service_demand_chart)
        return StreamingResponse(
            iter([content]),
            media_type="application/pdf",
            headers={"Content-Disposition": "attachment; filename=service-demand.pdf"},
        )
    except Exception as e:
        logging.error("Error generating service demand pdf: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")




@app.get("/api/reports/bicycle-demand.xlsx", tags=["Reports"])
async def report_bicycle_demand_excel(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_bicycle_demand_by_type()
        from report_exports import build_single_report_excel
        from graphs import generate_bicycle_type_demand_chart
        content = build_single_report_excel(data, "bicycle_demand", lang, generate_bicycle_type_demand_chart)
        return StreamingResponse(
            iter([content]),
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            headers={"Content-Disposition": "attachment; filename=bicycle-demand.xlsx"},
        )
    except Exception as e:
        logging.error("Error generating bicycle demand excel: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/reports/bicycle-demand.pdf", tags=["Reports"])
async def report_bicycle_demand_pdf(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_bicycle_demand_by_type()
        from report_exports import build_single_report_pdf
        from graphs import generate_bicycle_type_demand_chart
        content = build_single_report_pdf(data, "bicycle_demand", lang, generate_bicycle_type_demand_chart)
        return StreamingResponse(
            iter([content]),
            media_type="application/pdf",
            headers={"Content-Disposition": "attachment; filename=bicycle-demand.pdf"},
        )
    except Exception as e:
        logging.error("Error generating bicycle demand pdf: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")




@app.get("/api/reports/daily-trips.xlsx", tags=["Reports"])
async def report_daily_trips_excel(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_daily_trips()
        from report_exports import build_single_report_excel
        from graphs import generate_daily_trips_chart
        content = build_single_report_excel(data, "daily_trips", lang, generate_daily_trips_chart)
        return StreamingResponse(
            iter([content]),
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            headers={"Content-Disposition": "attachment; filename=daily-trips.xlsx"},
        )
    except Exception as e:
        logging.error("Error generating daily trips excel: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/reports/daily-trips.pdf", tags=["Reports"])
async def report_daily_trips_pdf(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_daily_trips()
        from report_exports import build_single_report_pdf
        from graphs import generate_daily_trips_chart
        content = build_single_report_pdf(data, "daily_trips", lang, generate_daily_trips_chart)
        return StreamingResponse(
            iter([content]),
            media_type="application/pdf",
            headers={"Content-Disposition": "attachment; filename=daily-trips.pdf"},
        )
    except Exception as e:
        logging.error("Error generating daily trips pdf: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")




@app.get("/api/reports/maintenances.xlsx", tags=["Reports"])
async def report_maintenances_excel(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_maintenances()
        from report_exports import build_single_report_excel
        # No graph for maintenances yet, or maybe a simple one? 
        # For now, no graph function passed.
        content = build_single_report_excel(data, "maintenances", lang)
        return StreamingResponse(
            iter([content]),
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            headers={"Content-Disposition": "attachment; filename=maintenances.xlsx"},
        )
    except Exception as e:
        logging.error("Error generating maintenances excel: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/reports/maintenances.pdf", tags=["Reports"])
async def report_maintenances_pdf(lang: str = "es"):
    aggregator = ReportAggregator()
    try:
        data = await aggregator.aggregate_maintenances()
        from report_exports import build_single_report_pdf
        content = build_single_report_pdf(data, "maintenances", lang)
        return StreamingResponse(
            iter([content]),
            media_type="application/pdf",
            headers={"Content-Disposition": "attachment; filename=maintenances.pdf"},
        )
    except Exception as e:
        logging.error("Error generating maintenances pdf: %s", e)
        raise HTTPException(status_code=500, detail="Internal server error")
