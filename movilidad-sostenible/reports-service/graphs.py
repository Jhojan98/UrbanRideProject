import io
from typing import Any, Dict, List, Optional

import matplotlib.pyplot as plt
import matplotlib.dates as mdates
from datetime import datetime

plt.switch_backend('Agg')

URBAN_GREEN_HEX = "#2E7D32"
URBAN_LIGHT_GREEN_HEX = "#E8F5E9"

def _setup_plot(title: str, xlabel: str = "", ylabel: str = ""):
    """Helper to setup common plot attributes."""
    plt.figure(figsize=(10, 6))
    plt.title(title, fontsize=14, fontweight='bold', color=URBAN_GREEN_HEX)
    if xlabel:
        plt.xlabel(xlabel, fontsize=12)
    if ylabel:
        plt.ylabel(ylabel, fontsize=12)
    plt.grid(axis='y', linestyle='--', alpha=0.7)
    plt.tight_layout()

def _save_plot_to_buffer() -> io.BytesIO:
    """Save the current plot to a BytesIO buffer."""
    buf = io.BytesIO()
    plt.savefig(buf, format='png', dpi=100, bbox_inches='tight')
    plt.close()
    buf.seek(0)
    return buf

def generate_bicycle_usage_chart(data: List[Dict[str, Any]], lang: str = "es") -> io.BytesIO:
    """Generate a bar chart for bicycle usage."""
    sorted_data = sorted(data, key=lambda x: x['usage_count'], reverse=True)[:20]
    
    ids = [str(item['bicycle_id']) for item in sorted_data]
    counts = [item['usage_count'] for item in sorted_data]
    
    title = "Frecuencia de Uso de Bicicletas (Top 20)" if lang == "es" else "Bicycle Usage Frequency (Top 20)"
    xlabel = "ID Bicicleta" if lang == "es" else "Bicycle ID"
    ylabel = "Cantidad de Viajes" if lang == "es" else "Trip Count"
    
    _setup_plot(title, xlabel, ylabel)
    bars = plt.bar(ids, counts, color=URBAN_GREEN_HEX)
    plt.xticks(rotation=45, ha='right')
    
    for bar in bars:
        height = bar.get_height()
        plt.text(bar.get_x() + bar.get_width()/2., height,
                 f'{int(height)}',
                 ha='center', va='bottom')
                 
    return _save_plot_to_buffer()

def generate_station_demand_chart(data: Dict[str, Any], lang: str = "es") -> io.BytesIO:
    """Generate a grouped bar chart for station demand (Origin vs Destination)."""
    origins = {item['station_id']: item['count'] for item in data.get('most_popular_origins', [])}
    destinations = {item['station_id']: item['count'] for item in data.get('most_popular_destinations', [])}
    
    all_stations = set(origins.keys()) | set(destinations.keys())
    
    total_traffic = {sid: origins.get(sid, 0) + destinations.get(sid, 0) for sid in all_stations}
    top_stations = sorted(total_traffic.items(), key=lambda x: x[1], reverse=True)[:10]
    top_ids = [sid for sid, _ in top_stations]
    
    origin_counts = [origins.get(sid, 0) for sid in top_ids]
    dest_counts = [destinations.get(sid, 0) for sid in top_ids]
    
    x = range(len(top_ids))
    width = 0.35
    
    title = "Demanda por Estación (Top 10)" if lang == "es" else "Station Demand (Top 10)"
    xlabel = "ID Estación" if lang == "es" else "Station ID"
    ylabel = "Cantidad de Viajes" if lang == "es" else "Trip Count"
    label_origin = "Origen" if lang == "es" else "Origin"
    label_dest = "Destino" if lang == "es" else "Destination"
    
    _setup_plot(title, xlabel, ylabel)
    plt.bar([i - width/2 for i in x], origin_counts, width, label=label_origin, color=URBAN_GREEN_HEX)
    plt.bar([i + width/2 for i in x], dest_counts, width, label=label_dest, color="#81C784") # Lighter green
    
    plt.xticks(x, top_ids, rotation=45)
    plt.legend()
    
    return _save_plot_to_buffer()

def generate_service_demand_chart(data: Dict[str, Any], lang: str = "es") -> io.BytesIO:
    """Generate a pie chart for service demand."""
    labels = ["Última Milla", "Recorrido Largo"] if lang == "es" else ["Last Mile", "Long Distance"]
    sizes = [data.get('last_mile_trips', 0), data.get('long_distance_trips', 0)]
    colors = [URBAN_GREEN_HEX, "#81C784"]
    
    title = "Demanda por Tipo de Servicio" if lang == "es" else "Service Demand by Type"
    
    plt.figure(figsize=(8, 6))
    plt.title(title, fontsize=14, fontweight='bold', color=URBAN_GREEN_HEX)
    
    if sum(sizes) > 0:
        plt.pie(sizes, labels=labels, colors=colors, autopct='%1.1f%%', startangle=140)
        plt.axis('equal')
    else:
        plt.text(0.5, 0.5, "No Data", ha='center', va='center')
        
    return _save_plot_to_buffer()

def generate_bicycle_type_demand_chart(data: Dict[str, Any], lang: str = "es") -> io.BytesIO:
    """Generate a pie chart for bicycle type demand."""
    labels = ["Eléctrica", "Mecánica"] if lang == "es" else ["Electric", "Mechanical"]
    sizes = [data.get('electric_usage', 0), data.get('mechanical_usage', 0)]
    colors = [URBAN_GREEN_HEX, "#A5D6A7"]
    
    title = "Demanda por Tipo de Bicicleta" if lang == "es" else "Bicycle Demand by Type"
    
    plt.figure(figsize=(8, 6))
    plt.title(title, fontsize=14, fontweight='bold', color=URBAN_GREEN_HEX)
    
    if sum(sizes) > 0:
        plt.pie(sizes, labels=labels, colors=colors, autopct='%1.1f%%', startangle=140)
        plt.axis('equal')
    else:
        plt.text(0.5, 0.5, "No Data", ha='center', va='center')
        
    return _save_plot_to_buffer()

def generate_daily_trips_chart(data: List[Dict[str, Any]], lang: str = "es") -> io.BytesIO:
    """Generate a line chart for daily trips."""
    sorted_data = sorted(data, key=lambda x: x['date'])
    
    dates = [item['date'] for item in sorted_data]
    counts = [item['count'] for item in sorted_data]
    
    title = "Viajes por Día" if lang == "es" else "Daily Trips"
    xlabel = "Fecha" if lang == "es" else "Date"
    ylabel = "Cantidad de Viajes" if lang == "es" else "Trip Count"
    
    _setup_plot(title, xlabel, ylabel)
    
    if dates:
        plt.plot(dates, counts, marker='o', linestyle='-', color=URBAN_GREEN_HEX, linewidth=2)
        plt.xticks(rotation=45)
    else:
        plt.text(0.5, 0.5, "No Data", ha='center', va='center')
        
    return _save_plot_to_buffer()
