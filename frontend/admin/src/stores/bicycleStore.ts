import { defineStore } from "pinia";
import type { AdminBicycle } from "@/models/AdminBicycle";

export const useBicycleStore = defineStore("bicycle", {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL,
    bicycles: [] as AdminBicycle[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    electricBicycles: (state) => state.bicycles.filter(b => b.model === "ELECTRIC"),
    mechanicBicycles: (state) => state.bicycles.filter(b => b.model === "MECHANIC"),
  },

  actions: {
    async fetchBicycles() {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/bicy/`, {
          method: "GET",
          headers: { Accept: "application/json" },
        });

        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }

        const data = await response.json();
        this.bicycles = data;
      } catch (error) {
        console.error("Error fetching bicycles:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        this.bicycles = [];
      } finally {
        this.loading = false;
      }
    },

    async createElectricBicycle(data: { series: number; padlockStatus: string; battery: number }) {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/bicy/electric`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify(data),
        });

        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }

        const created = await response.json();
        this.bicycles.push(created);
        return created;
      } catch (error) {
        console.error("Error creating electric bicycle:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async createMechanicBicycle(data: { series: number; padlockStatus: string }) {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/bicy/mechanic`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify(data),
        });

        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }

        const created = await response.json();
        this.bicycles.push(created);
        return created;
      } catch (error) {
        console.error("Error creating mechanic bicycle:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async deleteBicycle(id: string) {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/bicy/${id}`, {
          method: "DELETE",
          headers: { Accept: "application/json" },
        });

        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }

        this.bicycles = this.bicycles.filter(b => b.idBicycle !== id);
      } catch (error) {
        console.error("Error deleting bicycle:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    },
  },
});