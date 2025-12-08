/**
 * Modelo unificado de Bicicleta
 * Acepta campos del backend (idBicycle, padlockStatus, latitude, length)
 * y del frontend (id, lockStatus, lat, lon) sin necesidad de conversión
 */
export default interface Bike {
  // ID flexible: acepta idBicycle (backend) o id (frontend)
  idBicycle?: string;
  id?: string;

  series: number;
  model: "MECHANIC" | "ELECTRIC" | string;

  // Estado del candado flexible: acepta padlockStatus (backend) o lockStatus (frontend)
  padlockStatus?: string;
  lockStatus?: "LOCKED" | "UNLOCKED" | "ERROR" | string;

  // Ubicación flexible: acepta latitude/length (backend) o lat/lon (frontend)
  latitude?: number;
  lat?: number;
  length?: number;  // longitude en backend
  lon?: number;     // longitude en frontend

  battery?: number | string;

  // Timestamp flexible: acepta lastUpdate (backend) o timestamp (frontend)
  lastUpdate?: Date | string;
  timestamp?: Date;
}

/**
 * Helpers para acceso unificado a campos
 */
export const BikeHelpers = {
  getId(bike: Bike): string {
    return (bike.id || bike.idBicycle || '') as string;
  },

  getModel(bike: Bike): "MECHANIC" | "ELECTRIC" {
    const model = typeof bike.model === 'string' ? bike.model.toUpperCase() : bike.model;
    return (model === "ELECTRIC" || model === "ELÉCTRICA") ? "ELECTRIC" : "MECHANIC";
  },

  getLockStatus(bike: Bike): "LOCKED" | "UNLOCKED" | "ERROR" {
    const status = (bike.lockStatus || bike.padlockStatus || 'LOCKED').toUpperCase();
    if (status === "UNLOCKED" || status === "DESBLOQUEADO") return "UNLOCKED";
    if (status === "ERROR") return "ERROR";
    return "LOCKED";
  },

  getLat(bike: Bike): number {
    return bike.lat ?? bike.latitude ?? 0;
  },

  getLon(bike: Bike): number {
    return bike.lon ?? bike.length ?? 0;
  },

  getBattery(bike: Bike): string {
    if (bike.battery == null) return "0";
    return typeof bike.battery === 'string' ? bike.battery : bike.battery.toString();
  },

  getTimestamp(bike: Bike): Date | undefined {
    if (bike.timestamp instanceof Date) return bike.timestamp;
    if (bike.lastUpdate) {
      return bike.lastUpdate instanceof Date ? bike.lastUpdate : new Date(bike.lastUpdate);
    }
    return undefined;
  }
};

