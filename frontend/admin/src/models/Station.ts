/**
 * Estados de un slot de estación
 */
export enum SlotStatus {
  AVAILABLE = 'AVAILABLE',
  OCCUPIED = 'OCCUPIED',
  MAINTENANCE = 'MAINTENANCE',
  OUT_OF_SERVICE = 'OUT_OF_SERVICE'
}

/** Slot (ranura) dentro de una estación */
export interface Slot {
  idSlot: number;
  idStation: number;
  slotNumber: number;
  status: SlotStatus;
  idBicycle: number | null;
  lastUpdated: Date;
}

/**
 * Modelo unificado de Estación
 * Acepta campos de ambas convenciones: backend (stationName, length)
 * y admin (nameStation, longitude, length)
 */
export interface Station {
  idStation: number;

  // Nombre flexible: acepta nameStation o stationName
  nameStation?: string;
  stationName?: string;

  // Ubicación flexible: acepta latitude/longitude o latitude/length
  latitude: number;
  longitude?: number;
  length?: number;  // longitude en algunas respuestas del backend

  // Información general
  totalSlots?: number;
  availableSlots?: number;
  timestamp?: Date | string;
  slots?: Slot[];

  // Datos administrativos
  idCity?: number;
  type?: string;
  availableElectricBikes?: number;
  availableMechanicBikes?: number;
  cctvStatus?: boolean;
  lightingStatus?: boolean;
  panicButtonStatus?: boolean;
}

// DTO consolidado
export interface SlotDTO {
  idSlot: number;
  idStation: number;
  slotNumber: number;
  status: string;
  idBicycle: number | null;
  lastUpdated: string;
}

export interface StationDTO {
  idStation: number;
  nameStation?: string;
  stationName?: string;
  latitude: number;
  longitude?: number;
  length?: number;
  totalSlots?: number;
  availableSlots?: number;
  timestamp?: string;
  slots?: SlotDTO[];
  idCity?: number;
  type?: string;
  cctvStatus?: boolean;
}

// Conversores unificados
export function toSlot(dto: SlotDTO): Slot {
  return {
    idSlot: dto.idSlot,
    idStation: dto.idStation,
    slotNumber: dto.slotNumber,
    status: dto.status as SlotStatus,
    idBicycle: dto.idBicycle,
    lastUpdated: new Date(dto.lastUpdated)
  };
}

/**
 * Conversor único: maneja StationDTO con campos flexibles
 */
export function toStation(dto: StationDTO): Station {
  return {
    idStation: dto.idStation,
    nameStation: dto.nameStation,
    stationName: dto.stationName,
    latitude: dto.latitude,
    longitude: dto.longitude,
    length: dto.length,
    totalSlots: dto.totalSlots ?? 0,
    availableSlots: dto.availableSlots ?? 0,
    timestamp: dto.timestamp ? new Date(dto.timestamp) : new Date(),
    slots: dto.slots?.map(toSlot),
    idCity: dto.idCity,
    type: dto.type,
    cctvStatus: dto.cctvStatus
  };
}

/**
 * Helpers para acceso unificado a campos
 */
export const StationHelpers = {
  getName(station: Station): string {
    return (station.nameStation || station.stationName || '') as string;
  },

  getLongitude(station: Station): number {
    return station.longitude ?? station.length ?? 0;
  },

  getTimestamp(station: Station): Date {
    if (station.timestamp instanceof Date) return station.timestamp;
    if (typeof station.timestamp === 'string') return new Date(station.timestamp);
    return new Date();
  }
};
