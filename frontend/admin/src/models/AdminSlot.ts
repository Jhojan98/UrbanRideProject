/**
 * Modelo de Slot para GESTIÓN ADMINISTRATIVA
 */
export interface AdminSlot {
  idSlot: string;
  padlockStatus: string;
  stationId: number;
  bicycleId?: string | null;
}