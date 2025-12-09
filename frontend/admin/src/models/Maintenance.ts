export default interface Maintenance {
  entityType: "BICYCLE" | "STATION" | "LOCK";
  maintenanceType: "PREVENTIVE" | "CORRECTIVE" | "INSPECTION";
  triggeredBy: "ADMIN" | "IOT_ALERT";
  description: "string";
  status: "PENDING" | "SOLVING" | "RESOLVED";
  date: "2025-12-09";
  cost: number;
  bikeId: "string";
  stationId: number;
  lockId: "string";
  id:number
}
