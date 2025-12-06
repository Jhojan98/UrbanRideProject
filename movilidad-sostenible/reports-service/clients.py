import os
from typing import Any, Dict, List, Optional, Sequence, Set

import httpx


def _normalize_url(url: str) -> str:
    url = url.strip()
    if url and not url.startswith("http://") and not url.startswith("https://"):
        url = "http://" + url
    return url.rstrip("/")


class ServiceClients:
    """HTTP clients to talk to other microservices. URLs are configurable via env vars."""

    def __init__(self, timeout: float = 10.0) -> None:
        self.timeout = timeout
        self.users_base = _normalize_url(os.getenv("USERS_SERVICE_URL", "http://usuario-service:8100"))
        self.bicycle_base = _normalize_url(os.getenv("BICYCLE_SERVICE_URL", "http://bicis-service:8002"))
        # Keep legacy env var name for backwards compatibility.
        self.travel_base = _normalize_url(
            os.getenv("TRAVEL_SERVICE_URL")
            or os.getenv("RESERVATIONS_SERVICE_URL", "http://viaje-service:8003")
        )
        self.fines_base = _normalize_url(os.getenv("FINES_SERVICE_URL", "http://fine-service:5003"))
        self.complaints_base = _normalize_url(
            os.getenv("COMPLAINTS_SERVICE_URL", "http://complaints-service:5007")
        )
        self.maintenance_base = _normalize_url(
            os.getenv("MAINTENANCE_SERVICE_URL", "http://maintenance-service:5006")
        )
        self.stations_base = _normalize_url(
            os.getenv("STATION_SERVICE_URL", "http://estaciones-service:8005")
        )

    async def _get(self, url: str, params: Optional[Dict[str, Any]] = None) -> Any:
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            resp = await client.get(url, params=params)
            if resp.status_code == 204 or resp.content in (b"", None):
                return []
            resp.raise_for_status()
            try:
                return resp.json()
            except ValueError:
                return resp.text

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


    # Fines
    async def list_fines(self) -> List[Dict[str, Any]]:
        return await self._get(f"{self.fines_base}/api/fines")

    async def list_user_fines(self, user_id: str) -> List[Dict[str, Any]]:
        return await self._get(f"{self.fines_base}/api/user_fines/user/{user_id}")

    async def list_all_user_fines(self) -> List[Dict[str, Any]]:
        return await self._get(f"{self.fines_base}/api/user_fines")

    # Travels (Viajes)
    async def list_travels(self) -> List[Dict[str, Any]]:
        return await self._get(f"{self.travel_base}/")

    async def list_travels_by_user(self, user_id: str) -> List[Dict[str, Any]]:
        return await self._get(f"{self.travel_base}/usuario/{user_id}")

    # Maintenance (Mantenimientos) - Placeholder
    async def list_maintenances(self) -> List[Dict[str, Any]]:
        return await self._get(f"{self.maintenance_base}/maintenance/")

    # Complaints
    async def list_complaints(self, skip: int = 0, limit: int = 200) -> List[Dict[str, Any]]:
        params = {"skip": skip, "limit": limit}
        return await self._get(f"{self.complaints_base}/complaints/", params=params)

    async def list_complaints_by_travel_ids(self, travel_ids: Sequence[Any]) -> List[Dict[str, Any]]:
        ids: Set[int] = {
            int(tid)
            for tid in travel_ids
            if isinstance(tid, (int, str)) and str(tid).isdigit()
        }
        if not ids:
            return []
        complaints = await self.list_complaints(skip=0, limit=500)
        return [c for c in complaints if c.get("k_id_travel") in ids]

    # Stations
    async def get_station(self, station_id: Any) -> Dict[str, Any]:
        return await self._get(f"{self.stations_base}/stations/{station_id}")
