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

/** Modelo principal de estación (consolidado con slots opcionales) */
export interface Station {
  idStation: number;
  nameStation: string;
  latitude: number;
  longitude: number;
  totalSlots: number;
  availableSlots: number;
  timestamp: Date;
  slots?: Slot[]; // Opcional: presente cuando se cargan slots completos
  // Datos de panel admin
  availableElectricBikes?: number;
  availableMechanicBikes?: number;
  cctvStatus?: boolean;
  lightingStatus?: boolean;
  panicButtonStatus?: boolean;
}

// DTO consolidado (acepta con/sin slots)
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
  nameStation?: string; // API puede enviarlo como nameStation o stationName
  stationName?: string;
  latitude: number;
  longitude?: number;
  length?: number; // Algunas respuestas usan 'length' para longitud
  totalSlots?: number;
  availableSlots?: number;
  timestamp?: string;
  slots?: SlotDTO[]; // Opcional
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
 * Conversor único: maneja StationDTO con o sin slots
 */
export function toStation(dto: StationDTO): Station {
  return {
    idStation: dto.idStation,
    nameStation: dto.nameStation ?? dto.stationName ?? '',
    latitude: dto.latitude,
    longitude: dto.longitude ?? dto.length ?? 0,
    totalSlots: dto.totalSlots ?? 0,
    availableSlots: dto.availableSlots ?? 0,
    timestamp: new Date(dto.timestamp ?? Date.now()),
    slots: dto.slots?.map(toSlot)
  };
}
