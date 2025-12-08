/**
 * Modelo de Bicicleta para GESTIÓN ADMINISTRATIVA
 * (No confundir con Bicycle.ts que es para telemetría)
 */
export interface AdminBicycle {
  idBicycle: string;
  series: number;
  model: "ELECTRIC" | "MECHANIC";
  padlockStatus: string;
  lastUpdate?: Date;
  latitude?: number;
  length?: number;
  battery?: number;
}