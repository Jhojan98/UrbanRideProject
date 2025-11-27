from typing import Any, Dict, List

from clients import ServiceClients


class ReportAggregator:
    """Implements Aggregator Pattern to collect data from multiple services."""

    def __init__(self, clients: ServiceClients | None = None) -> None:
        self.clients = clients or ServiceClients()

    async def aggregate_overview(self) -> Dict[str, Any]:
        """Admin-level overview across services."""
        users = await self.clients.list_users()
        bicycles = await self.clients.list_bicycles()
        reservations = await self.clients.list_reservations()
        fines = await self.clients.list_fines()

        return {
            "users_count": len(users),
            "bicycles_count": len(bicycles),
            "reservations_count": len(reservations),
            "fines_count": len(fines),
            "users": users,
            "bicycles": bicycles,
            "reservations": reservations,
            "fines": fines,
        }

    async def aggregate_user_dashboard(self, user_id: str) -> Dict[str, Any]:
        """User-level dashboard aggregating personal info."""
        user = await self.clients.get_user(user_id)
        balance = await self.clients.get_user_balance(user_id)
        reservations = await self.clients.list_reservations_by_user(user_id)
        fines = await self.clients.list_user_fines(user_id)

        return {
            "user": user,
            "balance": balance,
            "reservations": reservations,
            "fines": fines,
            "reservations_count": len(reservations),
            "fines_count": len(fines),
        }
