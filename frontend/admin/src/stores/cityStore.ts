import { defineStore } from "pinia";
import type City from "@/models/City";

export const useCityStore = defineStore("city", {
  state: () => ({
    baseURL: '/api',
    cities: [] as City[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchCities() {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/city/`, {
          method: "GET",
          headers: { Accept: "application/json" },
        });
        if (!response.ok) {
          console.error(
            "HTTP error fetching cities:",
            response.status,
            response.statusText
          );
          this.cities = [];
          return;
        }
        const data = await response.json();
        this.cities = data;
      } catch (error) {
        console.error("Error fetching cities:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        this.cities = [];
      } finally {
        this.loading = false;
      }
    },

    async createCity(idCity: number, cityName: string) {
      this.loading = true;
      this.error = null;
      try {
        const token = localStorage.getItem('authToken');
        const headers: Record<string, string> = {
          "Content-Type": "application/json",
          Accept: "application/json",
        };
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`${this.baseURL}/city/`, {
          method: "POST",
          headers,
          body: JSON.stringify({
            idCity,
            cityName,
          }),
        });
        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status} ${response.statusText}`);
        }
        await this.fetchCities();
      } catch (error) {
        console.error("Error creating city:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async deleteCity(idCity: number) {
      this.loading = true;
      this.error = null;
      try {
        const token = localStorage.getItem('authToken');
        const headers: Record<string, string> = {
          Accept: "application/json",
        };
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
        }

        console.log(`Attempting to delete city with ID: ${idCity}`);
        console.log(`DELETE URL: ${this.baseURL}/city/delete/${idCity}`);

        const response = await fetch(`${this.baseURL}/city/delete/${idCity}`, {
          method: "DELETE",
          headers,
        });

        console.log(`Delete response status: ${response.status}`);

        if (!response.ok) {
          const errorText = await response.text();
          console.error(`Delete failed: ${response.status} - ${errorText}`);
          throw new Error(`HTTP error: ${response.status} - ${errorText || response.statusText}`);
        }
        await this.fetchCities();
      } catch (error) {
        console.error("Error deleting city:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
        throw error;
      } finally {
        this.loading = false;
      }
    }
  },
});

// Para compatibilidad con imports antiguos
export default useCityStore;
