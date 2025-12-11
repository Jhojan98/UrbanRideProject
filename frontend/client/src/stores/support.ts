import { defineStore } from "pinia";
import type { ComplaintPayload, ComplaintResponse } from "@/models/Complaint";

export const useSupportStore = defineStore("support", {
  state: () => ({
    complaintsBaseURL: '/api/complaints',
    reportsBaseURL: '/api/report/api/reports',
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
      } catch (err: unknown) {
        this.error = err instanceof Error ? err.message : "Error al enviar la queja";
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
      } catch (err: unknown) {
        this.error = err instanceof Error ? err.message : "No se pudo descargar el reporte";
        console.error("[SupportStore] downloadReport error:", err);
        return null;
      } finally {
        this.loading = false;
      }
    },

  },
});
