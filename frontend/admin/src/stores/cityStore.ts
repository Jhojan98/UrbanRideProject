import { defineStore } from "pinia";
import type City from "@/models/City";

const cityStore = defineStore("city", {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL,
    cities: [] as City[],
  }),
  actions: {
    async fetchCities() {
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
        this.cities = [];
      }
    },

    async createCity(idCity: number, cityName: string) {
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
        // Optionally, you can fetch the updated list of cities after creation
        await this.fetchCities();
      } catch (error) {
        console.error("Error creating city:", error);
      }
    },
    async deleteCity(idCity: number) {
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
        // Optionally, you can fetch the updated list of cities after deletion
        await this.fetchCities();
      } catch (error) {
        console.error("Error deleting city:", error);
      }
    }
  },
});

export default cityStore;
