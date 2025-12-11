import asyncio
import logging
import os
import socket
from typing import Optional

from py_eureka_client import eureka_client as _eureka_client


def _to_bool(value: Optional[str], default: bool = True) -> bool:
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "y", "on"}


def _build_register_kwargs() -> dict:
    eureka_host = os.getenv("EUREKA_HOST", "eureka-server")
    eureka_port = os.getenv("EUREKA_PORT", "8761")
    eureka_server_url = f"http://{eureka_host}:{eureka_port}/eureka/"

    app_name = os.getenv("EUREKA_APP_NAME", "btnpanic-service")
    service_port = int(os.getenv("SERVICE_PORT", "5008"))
    service_host = os.getenv("SERVICE_HOST") or socket.gethostname()
    service_ip = os.getenv("SERVICE_IP")
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

    return register_kwargs


async def start() -> Optional[object]:
    if not _to_bool(os.getenv("EUREKA_ENABLED"), True):
        logging.info("Eureka registration disabled by EUREKA_ENABLED flag")
        return None

    register_kwargs = _build_register_kwargs()

    try:
        # py_eureka_client.init is blocking, so we run it in a thread if needed,
        # but init_async is available in newer versions.
        # The other services used asyncio.to_thread(init), so we stick to that for consistency
        # unless we want to use init_async.
        # Let's check if other services used init or init_async.
        # reports-service used asyncio.to_thread(_eureka_client.init)
        # btnPanic was using init_async.
        # I will use init_async if available, or stick to the pattern.
        # Since I am standardizing, I will use the same pattern as other services (eureka.py).
        # Wait, other services `eureka.py` used `asyncio.to_thread(_eureka_client.init)`.
        # I will copy that pattern.
        
        handle = await asyncio.to_thread(_eureka_client.init, **register_kwargs)
        logging.info(
            "Registered FastAPI service '%s' with Eureka at %s",
            register_kwargs.get("app_name"),
            register_kwargs.get("eureka_server"),
        )
        return handle
    except Exception as exc:  # pylint: disable=broad-except
        logging.error("Failed to initialize Eureka client: %s", exc)
        return None


async def stop(handle: Optional[object]) -> None:
    if handle is None:
        return

    try:
        await asyncio.to_thread(handle.stop)
        logging.info("Eureka client stopped")
    except Exception as exc:  # pylint: disable=broad-except
        logging.warning("Error stopping Eureka client: %s", exc)
