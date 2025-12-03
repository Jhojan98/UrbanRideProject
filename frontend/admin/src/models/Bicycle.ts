/**
 * Bicycle - Modelo que representa una bicicleta
 */
export interface Bicycle {
    id: string;
    lat: number;
    lon: number;
    battery: string;
    timestamp: Date;
}

/**
 * BicycleDTO - DTO recibido del WebSocket
 */
export interface BicycleDTO {
    idBicycle: number;
    latitude: number;
    longitude: number;
    battery: number;
    timestamp: number; // long timestamp desde el backend
}

/**
 * Convierte el DTO del WebSocket a modelo Bicycle
 */
export function toBicycle(dto: BicycleDTO): Bicycle {
    return {
        id: dto.idBicycle.toString(),
        lat: dto.latitude,
        lon: dto.longitude,
        battery: dto.battery.toFixed(2),
        timestamp: new Date(dto.timestamp)
    };
}
