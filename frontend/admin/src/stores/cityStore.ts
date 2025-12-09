import { defineStore } from "pinia";
import type City from "@/models/City";

export const useCityStore = defineStore("city", {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL,
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
        const response = await fetch(`${this.baseURL}/city/`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify({
            idCity,
            cityName,
          }),
        });
        if (!response.ok) {
          console.error(
            "HTTP error creating city:",
            response.status,
            response.statusText
          );
          return;
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
        const response = await fetch(`${this.baseURL}/city/${idCity}`, {
          method: "DELETE",
          headers: {
            Accept: "application/json",
          },
        });
        if (!response.ok) {
          console.error(
            "HTTP error deleting city:",
            response.status,
            response.statusText
          );
          return;
        }
        await this.fetchCities();
      } catch (error) {
        console.error("Error deleting city:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    }
  },
});

// Para compatibilidad con imports antiguos
export default useCityStore;
