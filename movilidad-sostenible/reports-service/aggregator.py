from typing import Any, Dict, List

from clients import ServiceClients


class ReportAggregator:
    """Implements Aggregator Pattern to collect data from multiple services."""

    def __init__(self, clients: ServiceClients | None = None) -> None:
        self.clients = clients or ServiceClients()

    async def aggregate_overview(self) -> Dict[str, Any]:
        """Admin-level overview across services."""
        users = await self.clients.list_users()
        #bicycles = await self.clients.list_bicycles()
        #reservations = await self.clients.list_reservations()
        fines = await self.clients.list_fines()

        return {
            "users_count": len(users),
           # "bicycles_count": len(bicycles),
            #"reservations_count": len(reservations),
            "fines_count": len(fines),
            "users": users,
            # "bicycles": bicycles,
            #"reservations": reservations,
            "fines": fines,
        }

    async def aggregate_user_dashboard(self, user_id: str) -> Dict[str, Any]:
        """User-level dashboard aggregating personal info."""
        user = await self.clients.get_user(user_id)
        balance = await self.clients.get_user_balance(user_id)
        #reservations = await self.clients.list_reservations_by_user(user_id)
        fines = await self.clients.list_user_fines(user_id)

        return {
            "user": user,
            "balance": balance,
            #"reservations": reservations,
            "fines": fines,
            #"reservations_count": len(reservations),
            "fines_count": len(fines),
        }

    async def aggregate_bicycle_usage(self) -> List[Dict[str, Any]]:
        """Frecuencia de uso de bicicletas (viajes por bicicleta)."""
        travels = await self.clients.list_travels()
        usage_count = {}
        for travel in travels:
            bike_id = travel.get("idBicycle")
            if bike_id:
                usage_count[bike_id] = usage_count.get(bike_id, 0) + 1
        
        # Enrich with bicycle details if needed (optional, for now just returning IDs and counts)
        return [{"bicycle_id": k, "usage_count": v} for k, v in usage_count.items()]

    async def aggregate_station_demand(self) -> Dict[str, Any]:
        """Estaciones de mayor demanda (origen y destino)."""
        travels = await self.clients.list_travels()
        origin_counts = {}
        destination_counts = {}

        for travel in travels:
            from_station = travel.get("fromIdStation")
            to_station = travel.get("toIdStation")
            
            if from_station:
                origin_counts[from_station] = origin_counts.get(from_station, 0) + 1
            if to_station:
                destination_counts[to_station] = destination_counts.get(to_station, 0) + 1
        
        return {
            "most_popular_origins": [{"station_id": k, "count": v} for k, v in sorted(origin_counts.items(), key=lambda item: item[1], reverse=True)],
            "most_popular_destinations": [{"station_id": k, "count": v} for k, v in sorted(destination_counts.items(), key=lambda item: item[1], reverse=True)]
        }

    async def aggregate_service_demand(self) -> Dict[str, Any]:
        """Servicio con mayor demanda (última milla vs recorrido largo)."""
        travels = await self.clients.list_travels()
        last_mile_count = 0
        long_distance_count = 0
        
        # Threshold: 15 minutes (900 seconds)
        THRESHOLD_SECONDS = 900 

        for travel in travels:
            started_at = travel.get("startedAt")
            ended_at = travel.get("endedAt")
            
            if started_at and ended_at:
                try:
                    # Assuming ISO format strings, simple parsing might be needed if not handled by client
                    from datetime import datetime
                    start = datetime.fromisoformat(started_at)
                    end = datetime.fromisoformat(ended_at)
                    duration = (end - start).total_seconds()
                    
                    if duration <= THRESHOLD_SECONDS:
                        last_mile_count += 1
                    else:
                        long_distance_count += 1
                except Exception:
                    # Fallback or ignore invalid dates
                    continue

        return {
            "last_mile_trips": last_mile_count,
            "long_distance_trips": long_distance_count,
            "dominant_service": "Last Mile" if last_mile_count >= long_distance_count else "Long Distance"
        }

    async def aggregate_bicycle_demand_by_type(self) -> Dict[str, Any]:
        """Bicicleta de mayor demanda (Eléctrica o Mecánica)."""
        travels = await self.clients.list_travels()
        bicycles = await self.clients.list_bicycles()
        
        # Map bike ID to type (inferred from battery or model)
        bike_types = {}
        for bike in bicycles:
            bike_id = bike.get("idBicycle")
            # Logic: If battery > 0 or model implies electric, it's electric. Otherwise mechanical.
            # This is an assumption.
            is_electric = bike.get("battery", 0) > 0 or "electric" in str(bike.get("model", "")).lower()
            bike_types[bike_id] = "Electric" if is_electric else "Mechanical"

        electric_count = 0
        mechanical_count = 0

        for travel in travels:
            bike_id = travel.get("idBicycle")
            b_type = bike_types.get(bike_id, "Unknown")
            if b_type == "Electric":
                electric_count += 1
            elif b_type == "Mechanical":
                mechanical_count += 1
        
        return {
            "electric_usage": electric_count,
            "mechanical_usage": mechanical_count,
            "dominant_type": "Electric" if electric_count >= mechanical_count else "Mechanical"
        }

    async def aggregate_daily_trips(self) -> List[Dict[str, Any]]:
        """Viajes por dia."""
        travels = await self.clients.list_travels()
        daily_counts = {}

        for travel in travels:
            started_at = travel.get("startedAt")
            if started_at:
                date_str = started_at.split("T")[0] # Extract YYYY-MM-DD
                daily_counts[date_str] = daily_counts.get(date_str, 0) + 1
        
        return [{"date": k, "count": v} for k, v in sorted(daily_counts.items())]

    async def aggregate_maintenances(self) -> List[Dict[str, Any]]:
        """Mantenimientos."""
        return await self.clients.list_maintenances()
