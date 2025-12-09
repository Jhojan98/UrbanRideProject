import { defineStore } from "pinia";
import type Complaint from "@/models/Complaint";

export const useComplaintsStore = defineStore("complaints", {
  state: () => ({
    baseURL: '/api/complaints',
    complaints: [] as Complaint[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    openComplaints: (state) => state.complaints.filter(c => c.t_status === 'OPEN'),
    inProgressComplaints: (state) => state.complaints.filter(c => c.t_status === 'IN_PROGRESS'),
    resolvedComplaints: (state) => state.complaints.filter(c => c.t_status === 'RESOLVED'),
    closedComplaints: (state) => state.complaints.filter(c => c.t_status === 'CLOSED'),
  },
  actions: {
    async fetchComplaints() {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/complaints/`, {
          method: "GET",
          headers: { Accept: "application/json" },
        });
        if (!response.ok) {
          console.error("HTTP error fetching complaints:", response.status, response.statusText);
          this.complaints = [];
          return;
        }
        const data = await response.json();
        this.complaints = Array.isArray(data) ? data.map((item): Complaint => ({
          k_id_complaints_and_claims: item.k_id_complaints_and_claims,
          d_description: item.d_description,
          t_status: item.t_status,
          k_id_travel: item.k_id_travel,
          t_type: item.t_type,
          created_at: item.created_at ?? undefined,
          updated_at: item.updated_at ?? undefined,
          user_id: item.user_id ?? undefined,
          user_name: item.user_name ?? undefined
        })) : [];
      } catch (error) {
        console.error("Error fetching complaints:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        this.complaints = [];
      } finally {
        this.loading = false;
      }
    },

    async updateComplaintStatus(id: number, status: string) {
      this.error = null;
      try {
        console.log('[ComplaintsStore] Actualizando queja:', { id, status });
        const payload = { t_status: status };
        console.log('[ComplaintsStore] Payload:', JSON.stringify(payload));

        const response = await fetch(`${this.baseURL}/complaints/${id}`, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify(payload),
        });

        if (!response.ok) {
          const errorText = await response.text();
          console.error("HTTP error updating complaint status:", response.status, response.statusText, errorText);
          throw new Error(`Error ${response.status}: ${errorText}`);
        }

        const result = await response.json();
        console.log('[ComplaintsStore] Update successful:', result);

        // Actualizar solo el registro específico en lugar de recargar todo
        const index = this.complaints.findIndex(c => c.k_id_complaints_and_claims === id);
        if (index !== -1) {
          this.complaints[index] = result;
        }
      } catch (error) {
        console.error("Error updating complaint status:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      }
    },

    async createComplaint(payload: Partial<Complaint>) {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/complaints/`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify(payload),
        });
        if (!response.ok) {
          console.error("HTTP error creating complaint:", response.status, response.statusText);
          return;
        }
        await this.fetchComplaints();
      } catch (error) {
        console.error("Error creating complaint:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async deleteComplaint(id: number) {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/complaints/${id}`, {
          method: "DELETE",
          headers: { Accept: "application/json" },
        });
        if (!response.ok) {
          console.error("HTTP error deleting complaint:", response.status, response.statusText);
          return;
        }
        await this.fetchComplaints();
      } catch (error) {
        console.error("Error deleting complaint:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    },
  },
});

export default useComplaintsStore;
