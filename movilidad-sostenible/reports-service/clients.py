import asyncio
import logging
import os
from typing import Any, Dict, List, Optional, Sequence, Set

import httpx
from py_eureka_client import eureka_client

# Configure logging
logger = logging.getLogger(__name__)

class ServiceClients:
    """HTTP clients to talk to other microservices using Eureka for discovery."""

    def __init__(self, timeout: float = 10.0) -> None:
        self.timeout = timeout
        # Service names as registered in Eureka
        self.service_names = {
            "users": "usuario-service",
            "bicycles": "bicis-service",
            "travels": "viaje-service",
            "fines": "fine-service",
            "complaints": "complaints-service",
            "maintenance": "maintenance-service",
            "stations": "estaciones-service",
        }

    async def _resolve_url(self, service_key: str) -> str:
        """
        Resolves the base URL for a given service key using Eureka.
        """
        service_name = self.service_names.get(service_key)
        if not service_name:
            logger.error(f"Service key '{service_key}' not found in configuration.")
            raise ValueError(f"Unknown service key: {service_key}")

        eureka_host = os.getenv("EUREKA_HOST", "eureka-server")
        eureka_port = os.getenv("EUREKA_PORT", "8761")
        eureka_server_url = f"http://{eureka_host}:{eureka_port}/eureka/"

        try:
            # Get application from Eureka.
            # py_eureka_client.get_application is async.
            app = await eureka_client.get_application(eureka_server_url, service_name)
            
            # Walk through instances to find a reachable one (simple round-robin or first available)
            # For simplicity, we take the first UP instance
            for instance in app.instances:
                if instance.status == "UP":
                    # Construct URL. Assuming standard HTTP for now.
                    # instance.homePageUrl usually contains the full URL including port
                    url = instance.homePageUrl.rstrip("/")
                    logger.debug(f"Resolved {service_name} to {url}")
                    return url
            
            logger.error(f"No 'UP' instances found for service: {service_name}")
            raise RuntimeError(f"Service unavailable: {service_name}")

        except Exception as e:
            logger.error(f"Failed to resolve service '{service_name}': {e}")
            raise

    async def _get(self, service_key: str, path: str, params: Optional[Dict[str, Any]] = None) -> Any:
        """
        Generic GET request that resolves the service URL first.
        """
        try:
            base_url = await self._resolve_url(service_key)
            url = f"{base_url}{path}"
            
            logger.info(f"Requesting {url}")
            
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.get(url, params=params)
                if resp.status_code == 204 or resp.content in (b"", None):
                    return []
                resp.raise_for_status()
                try:
                    return resp.json()
                except ValueError:
                    return resp.text
        except httpx.HTTPStatusError as e:
            logger.error(f"HTTP error calling {service_key}: {e.response.status_code} - {e.response.text}")
            raise
        except httpx.RequestError as e:
            logger.error(f"Network error calling {service_key}: {e}")
            raise
        except Exception as e:
            logger.error(f"Unexpected error calling {service_key}: {e}")
            raise

    # Users
    async def get_user(self, user_id: str) -> Dict[str, Any]:
        return await self._get("users", f"/login/{user_id}")

    async def list_users(self) -> List[Dict[str, Any]]:
        return await self._get("users", "/")

    async def get_user_balance(self, user_id: str) -> Dict[str, Any]:
        return await self._get("users", f"/balance/{user_id}")

    # Bicycles
    async def list_bicycles(self) -> List[Dict[str, Any]]:
        return await self._get("bicycles", "/")

    # Fines
    async def list_fines(self) -> List[Dict[str, Any]]:
        return await self._get("fines", "/api/fines")

    async def list_user_fines(self, user_id: str) -> List[Dict[str, Any]]:
        return await self._get("fines", f"/api/user_fines/user/{user_id}")

    async def list_all_user_fines(self) -> List[Dict[str, Any]]:
        return await self._get("fines", "/api/user_fines")

    # Travels (Viajes)
    async def list_travels(self) -> List[Dict[str, Any]]:
        return await self._get("travels", "/")

    async def list_travels_by_user(self, user_id: str) -> List[Dict[str, Any]]:
        return await self._get("travels", f"/usuario/{user_id}")

    # Maintenance (Mantenimientos)
    async def list_maintenances(self) -> List[Dict[str, Any]]:
        return await self._get("maintenance", "/maintenance/")

    # Complaints
    async def list_complaints(self, skip: int = 0, limit: int = 200) -> List[Dict[str, Any]]:
        params = {"skip": skip, "limit": limit}
        return await self._get("complaints", "/complaints/", params=params)

    async def list_complaints_by_travel_ids(self, travel_ids: Sequence[Any]) -> List[Dict[str, Any]]:
        ids: Set[int] = {
            int(tid)
            for tid in travel_ids
            if isinstance(tid, (int, str)) and str(tid).isdigit()
        }
        if not ids:
            return []
        # Note: This might be inefficient if list_complaints fetches all. 
        # But keeping logic same as before.
        complaints = await self.list_complaints(skip=0, limit=500)
        return [c for c in complaints if c.get("k_id_travel") in ids]

    # Stations
    async def get_station(self, station_id: Any) -> Dict[str, Any]:
        return await self._get("stations", f"/{station_id}")
