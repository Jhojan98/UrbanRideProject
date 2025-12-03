import os
from typing import Any, Dict, List, Optional

import httpx


def _normalize_url(url: str) -> str:
    url = url.strip()
    if url and not url.startswith("http://") and not url.startswith("https://"):
        url = "http://" + url
    return url.rstrip("/")


class ServiceClients:
    """HTTP clients to talk to other microservices. URLs are configurable via env vars.
    """

    def __init__(self, timeout: float = 10.0) -> None:
        self.timeout = timeout
        self.users_base = _normalize_url(os.getenv("USERS_SERVICE_URL", "http://usuario-service:8001"))
        self.bicycle_base = _normalize_url(os.getenv("BICYCLE_SERVICE_URL", "http://bicis-service:8002"))
        self.reservations_base = _normalize_url(os.getenv("RESERVATIONS_SERVICE_URL", "http://viaje-service:8003"))
        self.fines_base = _normalize_url(os.getenv("FINES_SERVICE_URL", "http://fine-service:8004"))

    async def _get(self, url: str, params: Optional[Dict[str, Any]] = None) -> Any:
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            resp = await client.get(url, params=params)
            resp.raise_for_status()
            return resp.json()

    # Users
    async def get_user(self, user_id: str) -> Dict[str, Any]:
        return await self._get(f"{self.users_base}/login/{user_id}")

    async def list_users(self) -> List[Dict[str, Any]]:
        return await self._get(f"{self.users_base}/")

    async def get_user_balance(self, user_id: str) -> Dict[str, Any]:
        return await self._get(f"{self.users_base}/balance/{user_id}")

    # Bicycles
    async def list_bicycles(self) -> List[Dict[str, Any]]:
        return await self._get(f"{self.bicycle_base}/")

    # Reservations
    async def list_reservations(self) -> List[Dict[str, Any]]:
        return await self._get(f"{self.reservations_base}/")

    async def list_reservations_by_user(self, user_id: str) -> List[Dict[str, Any]]:
        return await self._get(f"{self.reservations_base}/viaje/usuario/{user_id}")

    # Fines
    async def list_fines(self) -> List[Dict[str, Any]]:
        return await self._get(f"{self.fines_base}/api/fines")

    async def list_user_fines(self, user_id: str) -> List[Dict[str, Any]]:
        return await self._get(f"{self.fines_base}/api/user_fines/user/{user_id}")

    # Travels (Viajes)
    async def list_travels(self) -> List[Dict[str, Any]]:
        return await self._get(f"{self.reservations_base}/")

    # Maintenance (Mantenimientos) - Placeholder
    async def list_maintenances(self) -> List[Dict[str, Any]]:
        # Placeholder: The endpoint might not exist yet.
        # return await self._get(f"{self.bicycle_base}/maintenances")
        return []
