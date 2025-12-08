/**
 * DTO de Bicicleta desde el backend
 */
export interface BikeDTO {
  idBicycle: string;
  series: number;
  model: string;
  padlockStatus: string;
  lastUpdate?: string;
  latitude?: number;
  length?: number;
  battery?: number;
}

/**
 * Bicycle - Modelo que representa una bicicleta
 */
export default interface Bike {
  id: string;
  series: number;
  model: "MECHANIC" | "ELECTRIC";
  lockStatus: "LOCKED" | "UNLOCKED" | "ERROR";
  lat: number;
  lon: number;
  battery: string;
  timestamp?: Date;
}

/**
 * Convierte un DTO de bicicleta del backend al modelo Bike
 */
export function toBike(dto: BikeDTO): Bike {
  // Normalizar el modelo
  let model: "MECHANIC" | "ELECTRIC" = "MECHANIC";
  if (dto.model) {
    const upperModel = dto.model.toUpperCase();
    if (upperModel === "ELECTRIC" || upperModel === "ELÉCTRICA") {
      model = "ELECTRIC";
    }
  }

  // Normalizar el estado del candado
  let lockStatus: "LOCKED" | "UNLOCKED" | "ERROR" = "LOCKED";
  if (dto.padlockStatus) {
    const upperStatus = dto.padlockStatus.toUpperCase();
    if (upperStatus === "UNLOCKED" || upperStatus === "DESBLOQUEADO") {
      lockStatus = "UNLOCKED";
    } else if (upperStatus === "ERROR") {
      lockStatus = "ERROR";
    }
  }

  return {
    id: dto.idBicycle,
    series: dto.series,
    model,
    lockStatus,
    lat: dto.latitude ?? 0,
    lon: dto.length ?? 0, // length en backend = longitude
    battery: dto.battery != null ? dto.battery.toString() : "0",
    timestamp: dto.lastUpdate ? new Date(dto.lastUpdate) : undefined,
  };
}

