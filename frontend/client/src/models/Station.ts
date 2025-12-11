/**
 * States of a station slot
 */
export enum SlotStatus {
  AVAILABLE = 'AVAILABLE',
  OCCUPIED = 'OCCUPIED',
  MAINTENANCE = 'MAINTENANCE',
  OUT_OF_SERVICE = 'OUT_OF_SERVICE'
}

/** Slot (slot/space) within a station */
export interface Slot {
  idSlot: number;
  idStation: number;
  slotNumber: number;
  status: SlotStatus;
  idBicycle: number | null;
  lastUpdated: Date;
}

/** Main station model (consolidated with optional slots) */
export interface Station {
  idStation: number;
  nameStation: string;
  latitude: number;
  longitude: number;
  totalSlots: number;
  availableSlots: number;
  timestamp: Date;
  mechanical?: number; // Available mechanical bikes
  electric?: number;   // Available electric bikes
  // Optional telemetry received by WS
  cctvStatus?: boolean;
  lockedPadlocks?: number;
  unlockedPadlocks?: number;
  availableElectricBikes?: number;
  availableMechanicBikes?: number;
  slots?: Slot[]; // Optional: present when full slots are loaded
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
  nameStation: string;
  latitude: number;
  longitude: number;
  totalSlots?: number;
  availableSlots?: number;
  timestamp: string | number;
  mechanical?: number;
  electric?: number;
  cctvStatus?: boolean;
  lockedPadlocks?: number;
  unlockedPadlocks?: number;
  availableElectricBikes?: number;
  availableMechanicBikes?: number;
  slots?: SlotDTO[]; // Opcional
}

// Unified converters
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
  const ts = typeof dto.timestamp === 'number' ? new Date(dto.timestamp) : new Date(dto.timestamp);
  const totalSlots = dto.totalSlots ?? ((dto.lockedPadlocks ?? 0) + (dto.unlockedPadlocks ?? 0));
  const availableSlots = dto.availableSlots ?? dto.unlockedPadlocks ?? 0;

  return {
    idStation: dto.idStation,
    nameStation: dto.nameStation,
    latitude: dto.latitude,
    longitude: dto.longitude,
    totalSlots,
    availableSlots,
    timestamp: ts,
    mechanical: dto.mechanical ?? dto.availableMechanicBikes ?? 0,
    electric: dto.electric ?? dto.availableElectricBikes ?? 0,
    cctvStatus: dto.cctvStatus,
    lockedPadlocks: dto.lockedPadlocks,
    unlockedPadlocks: dto.unlockedPadlocks,
    availableElectricBikes: dto.availableElectricBikes,
    availableMechanicBikes: dto.availableMechanicBikes,
    slots: dto.slots?.map(toSlot)
  };
}

/**
 * Telemetry data received via WebSocket
 */
export interface StationTelemetry {
  idStation?: number;
  timestamp?: number;
  availableElectricBikes?: number;
  availableMechanicBikes?: number;
}
