/**
 * Modelo de Estación para GESTIÓN ADMINISTRATIVA
 * (No confundir con Station.ts que es para telemetría)
 */
export interface AdminStation {
  idStation: number;
  stationName: string;
  latitude: number;
  length: number;
  idCity: number;
  type: string;
  cctvStatus: boolean;
}