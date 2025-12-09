import { defineStore } from "pinia";

export type ComplaintType = "BICYCLE" | "SLOT" | "STATION";
export type ComplaintStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED" | "PENDING";
export type MaintenanceType = "PREVENTIVE" | "CORRECTIVE" | "INSPECTION";
export type MaintenanceEntityType = "BICYCLE" | "STATION" | "LOCK";
export type MaintenanceTriggeredBy = "ADMIN" | "USER" | "IOT_ALERT";
export type MaintenanceStatus = "PENDING" | "SOLVING" | "RESOLVED";

export interface ComplaintPayload {
  description: string;
  type: ComplaintType;
  travelId?: number;
  status?: ComplaintStatus;
}

export interface ComplaintResponse {
  k_id_complaints_and_claims: number;
  d_description: string;
  t_status: ComplaintStatus;
  k_id_travel: number | null;
  t_type: ComplaintType;
}

export interface MaintenancePayload {
  entityType: MaintenanceEntityType;
  maintenanceType?: MaintenanceType;
  triggeredBy: MaintenanceTriggeredBy;
  description: string;
  status?: MaintenanceStatus;
  date: string;
  cost?: number;
  bikeId?: string;
  stationId?: number;
  lockId?: string;
}

export interface MaintenanceResponse {
  id: string;
  entityType: MaintenanceEntityType;
  maintenanceType: MaintenanceType;
  triggeredBy: MaintenanceTriggeredBy;
  description: string;
  status: MaintenanceStatus;
  date: string;
  cost?: number;
  bikeId?: string;
  stationId?: number;
  lockId?: string;
}

export const useSupportStore = defineStore("support", {
  state: () => ({
    complaintsBaseURL: process.env.VUE_APP_COMPLAINTS_URL || "http://localhost:5007",
    maintenanceBaseURL: process.env.VUE_APP_MAINTENANCE_URL || "http://localhost:5006",
    reportsBaseURL: process.env.VUE_APP_REPORTS_URL || "http://localhost:5004/api/reports",
    loading: false,
    error: null as string | null,
    lastComplaint: null as ComplaintResponse | null,
    lastMaintenance: null as MaintenanceResponse | null,
  }),

  actions: {
    async submitComplaint(payload: ComplaintPayload): Promise<ComplaintResponse | null> {
      this.loading = true;
      this.error = null;
      this.lastComplaint = null;

      const body = {
        d_description: payload.description,
        t_type: payload.type,
        k_id_travel: payload.travelId ?? null,
        t_status: payload.status || "OPEN",
      };

      try {
        const res = await fetch(`${this.complaintsBaseURL}/complaints/`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify(body),
        });

        if (!res.ok) {
          const text = await res.text().catch(() => "");
          throw new Error(`HTTP ${res.status} ${res.statusText} ${text}`);
        }

        const data = (await res.json()) as ComplaintResponse;
        this.lastComplaint = data;
        return data;
      } catch (err: any) {
        this.error = err?.message || "Error al enviar la queja";
        console.error("[SupportStore] submitComplaint error:", err);
        return null;
      } finally {
        this.loading = false;
      }
    },

    async downloadReport(file: string): Promise<Blob | null> {
      this.loading = true;
      this.error = null;
      try {
        const url = `${this.reportsBaseURL}/${file}`;
        const res = await fetch(url);
        if (!res.ok) {
          const txt = await res.text().catch(() => "");
          throw new Error(`HTTP ${res.status} ${res.statusText} ${txt}`);
        }
        return await res.blob();
      } catch (err: any) {
        this.error = err?.message || "No se pudo descargar el reporte";
        console.error("[SupportStore] downloadReport error:", err);
        return null;
      } finally {
        this.loading = false;
      }
    },

    async submitMaintenance(payload: MaintenancePayload): Promise<MaintenanceResponse | null> {
      this.loading = true;
      this.error = null;
      this.lastMaintenance = null;

      const body = {
        entityType: payload.entityType,
        maintenanceType: payload.maintenanceType || "PREVENTIVE",
        triggeredBy: payload.triggeredBy,
        description: payload.description,
        status: payload.status || "PENDING",
        date: payload.date,
        cost: payload.cost || null,
        bikeId: payload.bikeId || null,
        stationId: payload.stationId || null,
        lockId: payload.lockId || null,
      };

      try {
        const res = await fetch(`${this.maintenanceBaseURL}/maintenance/`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Accept": "application/json",
          },
          body: JSON.stringify(body),
        });

        if (!res.ok) {
          const text = await res.text().catch(() => "");
          throw new Error(`HTTP ${res.status} ${res.statusText} ${text}`);
        }

        const data = (await res.json()) as MaintenanceResponse;
        this.lastMaintenance = data;
        return data;
      } catch (err: any) {
        this.error = err?.message || "Error al enviar solicitud de mantenimiento";
        console.error("[SupportStore] submitMaintenance error:", err);
        return null;
      } finally {
        this.loading = false;
      }
    },
  },
});
