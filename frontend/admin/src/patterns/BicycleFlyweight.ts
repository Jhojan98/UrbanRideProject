import { Marker, LatLngExpression, DivIcon } from 'leaflet';
import { Bicycle } from '@/models/Bicycle';

/**
 * BicycleFlyweight - Estado intrínseco (compartido) de las bicicletas
 * Contiene datos que NO cambian entre instancias (el ícono del marcador)
 */
class BicycleFlyweight {
    private static readonly icon: DivIcon = new DivIcon({
        html: `
            <div style="
                width: 32px; 
                height: 32px; 
                background: #4CAF50; 
                border: 3px solid white; 
                border-radius: 50%; 
                display: flex; 
                align-items: center; 
                justify-content: center;
                box-shadow: 0 2px 5px rgba(0,0,0,0.3);
                font-size: 18px;
            ">🚲</div>
        `,
        className: 'bicycle-marker',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    });

    // Ícono alternativo para bicicletas con batería baja
    private static readonly lowBatteryIcon: DivIcon = new DivIcon({
        html: `
            <div style="
                width: 32px; 
                height: 32px; 
                background: #f44336; 
                border: 3px solid white; 
                border-radius: 50%; 
                display: flex; 
                align-items: center; 
                justify-content: center;
                box-shadow: 0 2px 5px rgba(0,0,0,0.3);
                font-size: 18px;
                animation: pulse 1.5s infinite;
            ">🚲</div>
            <style>
                @keyframes pulse {
                    0%, 100% { transform: scale(1); }
                    50% { transform: scale(1.1); }
                }
            </style>
        `,
        className: 'bicycle-marker-low',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    });

    /**
     * Obtiene el ícono compartido basado en el nivel de batería
     */
    public static getIcon(battery: string): DivIcon {
        const batteryLevel = parseInt(battery);
        return batteryLevel < 20 ? this.lowBatteryIcon : this.icon;
    }
}

/**
 * BicycleMarker - Estado extrínseco (único por bicicleta)
 * Contiene datos que SÍ cambian entre instancias
 */
export class BicycleMarker {
    private bicycle: Bicycle;
    private marker: Marker | null = null;

    constructor(bicycle: Bicycle) {
        this.bicycle = bicycle;
    }

    /**
     * Crea o actualiza el marcador en el mapa
     */
    public render(map: L.Map): Marker {
        const position: LatLngExpression = [this.bicycle.lat, this.bicycle.lon];
        
        if (!this.marker) {
            // Crear nuevo marcador usando el Flyweight (ícono compartido)
            this.marker = new Marker(position, {
                icon: BicycleFlyweight.getIcon(this.bicycle.battery)
            });

            this.marker.addTo(map);
        } else {
            // Actualizar posición del marcador existente
            this.marker.setLatLng(position);
            this.marker.setIcon(BicycleFlyweight.getIcon(this.bicycle.battery));
        }

        // Actualizar popup con información de la bicicleta
        const popupContent = this.createPopupContent();
        this.marker.bindPopup(popupContent);

        return this.marker;
    }

    /**
     * Actualiza los datos de la bicicleta
     */
    public update(bicycle: Bicycle): void {
        this.bicycle = bicycle;
    }

    /**
     * Obtiene el ID de la bicicleta
     */
    public getId(): string {
        return this.bicycle.id;
    }

    /**
     * Obtiene los datos de la bicicleta
     */
    public getBicycle(): Bicycle {
        return this.bicycle;
    }

    /**
     * Elimina el marcador del mapa
     */
    public remove(): void {
        if (this.marker) {
            this.marker.remove();
            this.marker = null;
        }
    }

    /**
     * Crea el contenido HTML del popup
     */
    private createPopupContent(): string {
        const date = this.bicycle.timestamp;
        const formattedDate = date.toLocaleDateString('es-CO', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
        const formattedTime = date.toLocaleTimeString('es-CO', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });

        const batteryLevel = parseInt(this.bicycle.battery);
        const batteryColor = batteryLevel < 20 ? '#f44336' : batteryLevel < 50 ? '#ff9800' : '#4caf50';

        return `
            <div style="font-family: Arial, sans-serif; min-width: 180px;">
                <h3 style="margin: 0 0 10px 0; color: #2c3e50; font-size: 16px;">
                    🚲 Bicicleta ${this.bicycle.id}
                </h3>
                <div style="font-size: 13px; color: #555;">
                    <p style="margin: 5px 0;">
                        <strong>Batería:</strong> 
                        <span style="color: ${batteryColor}; font-weight: bold;">
                            ${this.bicycle.battery}%
                        </span>
                    </p>
                    <p style="margin: 5px 0;">
                        <strong>Ubicación:</strong><br/>
                        Lat: ${this.bicycle.lat.toFixed(6)}<br/>
                        Lon: ${this.bicycle.lon.toFixed(6)}
                    </p>
                    <p style="margin: 5px 0; font-size: 11px; color: #888;">
                        <strong>Última actualización:</strong><br/>
                        ${formattedDate} ${formattedTime}
                    </p>
                </div>
            </div>
        `;
    }
}

/**
 * BicycleFactory - Gestor del patrón Flyweight
 * Mantiene un pool de BicycleMarker para reutilizarlos
 */
export class BicycleFactory {
    private bicycleMarkers: Map<string, BicycleMarker> = new Map();

    /**
     * Obtiene o crea un BicycleMarker
     */
    public getBicycleMarker(bicycle: Bicycle): BicycleMarker {
        let marker = this.bicycleMarkers.get(bicycle.id);

        if (marker) {
            // Reutilizar marcador existente y actualizar sus datos
            marker.update(bicycle);
        } else {
            // Crear nuevo marcador
            marker = new BicycleMarker(bicycle);
            this.bicycleMarkers.set(bicycle.id, marker);
        }

        return marker;
    }

    /**
     * Elimina un BicycleMarker del pool
     */
    public removeBicycleMarker(bicycleId: string): void {
        const marker = this.bicycleMarkers.get(bicycleId);
        if (marker) {
            marker.remove();
            this.bicycleMarkers.delete(bicycleId);
        }
    }

    /**
     * Obtiene todos los marcadores activos
     */
    public getAllMarkers(): BicycleMarker[] {
        return Array.from(this.bicycleMarkers.values());
    }

    /**
     * Limpia todos los marcadores
     */
    public clear(): void {
        this.bicycleMarkers.forEach(marker => marker.remove());
        this.bicycleMarkers.clear();
    }

    /**
     * Obtiene la cantidad de marcadores en el pool
     */
    public size(): number {
        return this.bicycleMarkers.size;
    }
}
