import * as L from 'leaflet';
import type Bike  from '@/models/Bike';

/**
 * BicycleFlyweight - Estado intrínseco (compartido) de las bicicletas
 * Contiene datos que NO cambian entre instancias (el ícono del marcador)
 */
class BicycleFlyweight {
    private static readonly icon: L.DivIcon = L.divIcon({
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
    private static readonly lowBatteryIcon: L.DivIcon = L.divIcon({
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

    // Ícono para bicicletas mecánicas
    private static readonly mechanicIcon: L.DivIcon = L.divIcon({
        html: `
            <div style="
                width: 32px;
                height: 32px;
                background: #2196F3;
                border: 3px solid white;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                box-shadow: 0 2px 5px rgba(0,0,0,0.3);
                font-size: 18px;
            ">🚲</div>
        `,
        className: 'bicycle-marker-mechanic',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    });

    // Ícono para bicicletas bloqueadas/con error
    private static readonly lockedIcon: L.DivIcon = L.divIcon({
        html: `
            <div style="
                width: 32px;
                height: 32px;
                background: #9E9E9E;
                border: 3px solid white;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                box-shadow: 0 2px 5px rgba(0,0,0,0.3);
                font-size: 18px;
            ">🔒</div>
        `,
        className: 'bicycle-marker-locked',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    });

    /**
     * Obtiene el ícono compartido basado en el tipo y estado de la bicicleta
     */
    public static getIcon(bike: Bike): L.DivIcon {
        // Si está bloqueada o con error, mostrar ícono de bloqueo
        if (bike.lockStatus === 'LOCKED' || bike.lockStatus === 'ERROR') {
            return this.lockedIcon;
        }

        // Si es mecánica, usar ícono azul
        if (bike.model === 'MECHANIC') {
            return this.mechanicIcon;
        }

        // Si es eléctrica, verificar batería
        const batteryLevel = parseInt(bike.battery || '100');
        return batteryLevel < 20 ? this.lowBatteryIcon : this.icon;
    }
}

/**
 * BicycleMarker - Estado extrínseco (único por bicicleta)
 * Contiene datos que SÍ cambian entre instancias
 */
export class BicycleMarker {
    private bicycle: Bike;
    private marker: L.Marker | null = null;

    constructor(bicycle: Bike) {
        this.bicycle = bicycle;
    }

    /**
     * Crea o actualiza el marcador en el mapa
     */
    public render(map: L.Map): L.Marker {
        const position: L.LatLngExpression = [this.bicycle.lat, this.bicycle.lon];

        if (!this.marker) {
            // Crear nuevo marcador usando el Flyweight (ícono compartido)
            this.marker = L.marker(position, { icon: BicycleFlyweight.getIcon(this.bicycle) });

            this.marker.addTo(map);
        } else {
            // Actualizar posición del marcador existente
            this.marker.setLatLng(position);
            this.marker.setIcon(BicycleFlyweight.getIcon(this.bicycle));
        }

        // Actualizar popup con información de la bicicleta
        const popupContent = this.createPopupContent();
        this.marker.bindPopup(popupContent);

        return this.marker;
    }

    /**
     * Actualiza los datos de la bicicleta
     */
    public update(bicycle: Bike): void {
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
    public getBicycle(): Bike {
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
        const modelText = this.bicycle.model === 'ELECTRIC' ? 'Eléctrica' : 'Mecánica';
        const modelIcon = this.bicycle.model === 'ELECTRIC' ? '⚡' : '🔧';

        let lockStatusText = '';
        let lockStatusColor = '';
        switch (this.bicycle.lockStatus) {
            case 'UNLOCKED':
                lockStatusText = 'Desbloqueada';
                lockStatusColor = '#4caf50';
                break;
            case 'LOCKED':
                lockStatusText = 'Bloqueada';
                lockStatusColor = '#9E9E9E';
                break;
            case 'ERROR':
                lockStatusText = 'Error';
                lockStatusColor = '#f44336';
                break;
        }

        let batterySection = '';
        if (this.bicycle.model === 'ELECTRIC') {
            const batteryLevel = parseInt(this.bicycle.battery || '0');
            const batteryColor = batteryLevel < 20 ? '#f44336' : batteryLevel < 50 ? '#ff9800' : '#4caf50';
            batterySection = `
                <p style="margin: 5px 0;">
                    <strong>Batería:</strong>
                    <span style="color: ${batteryColor}; font-weight: bold;">
                        ${this.bicycle.battery}%
                    </span>
                </p>
            `;
        }

        let timestampSection = '';
        if (this.bicycle.timestamp) {
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
            timestampSection = `
                <p style="margin: 5px 0; font-size: 11px; color: #888;">
                    <strong>Última actualización:</strong><br/>
                    ${formattedDate} ${formattedTime}
                </p>
            `;
        }

        return `
            <div style="font-family: Arial, sans-serif; min-width: 200px;">
                <h3 style="margin: 0 0 10px 0; color: #2c3e50; font-size: 16px;">
                    🚲 Bicicleta #${this.bicycle.series}
                </h3>
                <div style="font-size: 13px; color: #555;">
                    <p style="margin: 5px 0;">
                        <strong>ID:</strong> ${this.bicycle.id}
                    </p>
                    <p style="margin: 5px 0;">
                        <strong>Tipo:</strong> ${modelIcon} ${modelText}
                    </p>
                    <p style="margin: 5px 0;">
                        <strong>Estado:</strong>
                        <span style="color: ${lockStatusColor}; font-weight: bold;">
                            ${lockStatusText}
                        </span>
                    </p>
                    ${batterySection}
                    <p style="margin: 5px 0;">
                        <strong>Ubicación:</strong><br/>
                        Lat: ${this.bicycle.lat.toFixed(6)}<br/>
                        Lon: ${this.bicycle.lon.toFixed(6)}
                    </p>
                    ${timestampSection}
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
    public getBicycleMarker(bicycle: Bike): BicycleMarker {
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
