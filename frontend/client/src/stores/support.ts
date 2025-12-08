import { defineStore } from "pinia";

export type ComplaintType = "BICYCLE" | "SLOT" | "STATION";
export type ComplaintStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED" | "PENDING";

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

export const useSupportStore = defineStore("support", {
  state: () => ({
    complaintsBaseURL: process.env.VUE_APP_COMPLAINTS_URL || "http://localhost:5007",
    maintenanceBaseURL: process.env.VUE_APP_MAINTENANCE_URL || "http://localhost:5006",
    reportsBaseURL: process.env.VUE_APP_REPORTS_URL || "http://localhost:5004/api/reports",
    loading: false,
    error: null as string | null,
    lastComplaint: null as ComplaintResponse | null,
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
  },
});
