from datetime import datetime
from typing import Any, Dict, List, Optional

from clients import ServiceClients


def _count_by_key(items: List[Dict[str, Any]], key: str) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    for item in items:
        value = item.get(key)
        if value is None:
            continue
        counts[str(value)] = counts.get(str(value), 0) + 1
    return counts


def _simplify_user_fines(fines: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """Keep only fine description/type plus user_fine reference if present."""
    simplified: List[Dict[str, Any]] = []
    for fine in fines:
        fine_field = fine.get("fine")
        if isinstance(fine_field, dict):
            fine_type = fine_field.get("d_description") or fine_field.get("description") or str(fine_field)
        else:
            fine_type = fine_field
        n_reason = fine.get("n_reason")
        v_amount_snapshot = fine.get("v_amount_snapshot")
        f_assigned_at = fine.get("f_assigned_at")
        simplified.append({
            "description": fine_type,
            "reason": n_reason,
            "amount_snapshot": v_amount_snapshot,
            "assigned_at": _fmt_date(f_assigned_at),
            "user_id": fine.get("k_uid_user"),
            "status": fine.get("t_state"),
        })
    return simplified


def _fmt_date(dt_str: Optional[str]) -> Optional[str]:
    if not dt_str:
        return None
    try:
        return datetime.fromisoformat(dt_str).date().isoformat()
    except Exception:
        return dt_str


def _fmt_time(dt_str: Optional[str]) -> Optional[str]:
    if not dt_str:
        return None
    try:
        return datetime.fromisoformat(dt_str).time().strftime("%H:%M")
    except Exception:
        return dt_str

def _shorten_date(dt_str: Optional[str]) -> Optional[str]:
    """Format date as YYYY-MM-DD HH:MM or YYYY-MM-DD if no time."""
    if not dt_str:
        return None
    try:
        dt = datetime.fromisoformat(dt_str)
        return dt.strftime("%Y-%m-%d %H:%M")
    except Exception:
        return dt_str

def _simplify_list_dates(items: List[Dict[str, Any]], date_fields: List[str]) -> List[Dict[str, Any]]:
    """In-place update of date fields in a list of dicts."""
    for item in items:
        for field in date_fields:
            if field in item:
                item[field] = _shorten_date(item[field])
    return items


def _simplify_user_travels(
    travels: List[Dict[str, Any]],
    station_names: Dict[Any, str],
    lang: str = "en",
) -> List[Dict[str, Any]]:
    """User-facing travel summary without internal IDs, localized."""
    cleaned: List[Dict[str, Any]] = []
    for travel in travels:
        started_at = travel.get("startedAt") or travel.get("f_started_at")
        ended_at = travel.get("endedAt") or travel.get("f_ended_at")
        required_at = travel.get("requiredAt") or travel.get("f_required_at")

        origin_id = travel.get("fromIdStation") or travel.get("k_from_id_station")
        dest_id = travel.get("toIdStation") or travel.get("k_to_id_station")
        origin_name = station_names.get(origin_id) or origin_id
        dest_name = station_names.get(dest_id) or dest_id

        base = {
            "date": _fmt_date(started_at) or _fmt_date(required_at),
            "start_time": _fmt_time(started_at),
            "end_time": _fmt_time(ended_at),
            "status": travel.get("status") or travel.get("t_status"),
            "bicycle": travel.get("idBicycle") or travel.get("k_id_bicycle"),
            "origin_station": origin_name,
            "destination_station": dest_name,
            "travel_type": travel.get("travelType") or travel.get("t_travel_type"),
        }

        if lang == "es":
            cleaned.append(
                {
                    "fecha": base["date"],
                    "hora_salida": base["start_time"],
                    "hora_llegada": base["end_time"],
                    "estado": base["status"],
                    "bicicleta": base["bicycle"],
                    "estacion_origen": base["origin_station"],
                    "estacion_destino": base["destination_station"],
                    "tipo_viaje": base["travel_type"],
                }
            )
        else:
            cleaned.append(base)
    return cleaned


async def _build_station_name_map(clients: ServiceClients, travels: List[Dict[str, Any]]) -> Dict[Any, str]:
    """Fetch station names once for all travels."""
    station_ids = set()
    for t in travels:
        sid = t.get("fromIdStation") or t.get("k_from_id_station")
        if sid is not None:
            station_ids.add(sid)
        sid = t.get("toIdStation") or t.get("k_to_id_station")
        if sid is not None:
            station_ids.add(sid)

    names: Dict[Any, str] = {}
    for sid in station_ids:
        try:
            station = await clients.get_station(sid)
            name = station.get("stationName") or station.get("n_station_name")
            if name:
                names[sid] = name
        except Exception:
            continue
    return names


class ReportAggregator:
    """Implements Aggregator Pattern to collect data from multiple services."""

    def __init__(self, clients: ServiceClients | None = None) -> None:
        self.clients = clients or ServiceClients()

    async def aggregate_overview(self, lang: str = "es") -> Dict[str, Any]:
        """Admin-level overview across services."""
        users = await self.clients.list_users()
        bicycles = await self.clients.list_bicycles()
        travels = await self.clients.list_travels()
        fine_types = await self.clients.list_fines()
        user_fines = _simplify_user_fines(await self.clients.list_all_user_fines())
        complaints = await self.clients.list_complaints()
        maintenances = await self.clients.list_maintenances()

        # Format dates for admin overview
        _simplify_list_dates(bicycles, ["f_last_update"])
        _simplify_list_dates(travels, ["f_started_at", "f_ended_at", "f_required_at", "startedAt", "endedAt", "requiredAt"])
        _simplify_list_dates(maintenances, ["f_date", "date"])
        
        return {
            "lang": lang,
            "users_count": len(users),
            "bicycles_count": len(bicycles),
            "travels_count": len(travels),
            "fines_count": len(user_fines),
            "complaints_count": len(complaints),
            "maintenance_count": len(maintenances),
            "complaints_by_status": _count_by_key(complaints, "t_status"),
            "complaints_by_type": _count_by_key(complaints, "t_type"),
            "maintenance_by_entity": _count_by_key(maintenances, "t_entity_tipe"),
            "users": users,
            "bicycles": bicycles,
            "travels": travels,
            "fine_types": fine_types,
            "user_fines": user_fines,
            "complaints": complaints,
            "maintenances": maintenances,
        }

    async def aggregate_user_dashboard(self, user_id: str, lang: str = "es") -> Dict[str, Any]:
        """User-level dashboard aggregating personal info."""
        user = await self.clients.get_user(user_id)
        # Remove sensitive/internal ID fields for the report
        for k in ["k_uid_user", "id", "user_id", "password"]:
            user.pop(k, None)

        balance = await self.clients.get_user_balance(user_id)
        travels_raw = await self.clients.list_travels_by_user(user_id)
        station_names = await _build_station_name_map(self.clients, travels_raw)
        
        # Use the requested language
        travels = _simplify_user_travels(travels_raw, station_names, lang=lang)
        
        travel_ids = [
            t.get("idTravel")
            or t.get("k_id_travel")
            or t.get("id")
            for t in travels_raw
        ]
        fines = _simplify_user_fines(await self.clients.list_user_fines(user_id))
        # Remove user_id from fines for dashboard as it is redundant and internal
        for f in fines:
            f.pop("user_id", None)

        complaints = await self.clients.list_complaints_by_travel_ids(travel_ids)

        return {
            "lang": lang,
            "user": user,
            "balance": balance,
            "travels": travels,
            "travels_count": len(travels),
            "fines": fines,
            "fines_count": len(fines),
            "complaints": complaints,
            "complaints_count": len(complaints),
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

    async def aggregate_complaints_summary(self) -> Dict[str, Any]:
        """Resumen de quejas para administradores."""
        complaints = await self.clients.list_complaints()
        return {
            "complaints": complaints,
            "count": len(complaints),
            "by_status": _count_by_key(complaints, "t_status"),
            "by_type": _count_by_key(complaints, "t_type"),
        }

    async def aggregate_user_complaints(self, user_id: str) -> Dict[str, Any]:
        """Quejas asociadas a los viajes de un usuario."""
        travels = await self.clients.list_travels_by_user(user_id)
        travel_ids = [
            t.get("idTravel")
            or t.get("k_id_travel")
            or t.get("id")
            for t in travels
        ]
        complaints = await self.clients.list_complaints_by_travel_ids(travel_ids)
        return {
            "travels_count": len(travels),
            "complaints": complaints,
            "complaints_count": len(complaints),
            "by_status": _count_by_key(complaints, "t_status"),
            "by_type": _count_by_key(complaints, "t_type"),
        }
