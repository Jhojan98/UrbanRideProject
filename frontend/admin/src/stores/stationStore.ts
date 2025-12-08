import { defineStore } from "pinia";
import type { Station } from "@/models/Station";
import type { AdminSlot } from "@/models/AdminSlot";

export const useStationStore = defineStore("station", {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL,
    stations: [] as Station[],
    slots: [] as AdminSlot[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    getStationById: (state) => (id: number) => state.stations.find(s => s.idStation === id),
    getSlotsByStation: (state) => (stationId: number) =>
      state.slots.filter(slot => slot.stationId === stationId),
  },

  actions: {
    async fetchStations() {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/station/`, {
          method: "GET",
          headers: { Accept: "application/json" },
        });

        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }

        const data = await response.json();
        this.stations = data;
      } catch (error) {
        console.error("Error fetching stations:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        this.stations = [];
      } finally {
        this.loading = false;
      }
    },

    async createStation(data: {
      idStation: number;
      stationName: string;
      latitude: number;
      length: number;
      idCity: number;
      type: string;
      cctvStatus: boolean;
    }) {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/station/`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify(data),
        });

        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.mensaje || `HTTP error: ${response.status}`);
        }

        const result = await response.json();
        // result contiene {estacion: Station, slotsGenerados: [...]}
        this.stations.push(result.estacion);
        await this.fetchStations();
        await this.fetchSlots();
        return result;
      } catch (error) {
        console.error("Error creating station:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async deleteStation(id: number) {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/station/${id}`, {
          method: "DELETE",
          headers: { Accept: "application/json" },
        });

        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }

        this.stations = this.stations.filter(s => s.idStation !== id);
        this.slots = this.slots.filter(slot => slot.stationId !== id);
      } catch (error) {
        console.error("Error deleting station:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async fetchSlots() {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/slot/`, {
          method: "GET",
          headers: { Accept: "application/json" },
        });

        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }

        const data = await response.json();
        this.slots = data;
      } catch (error) {
        console.error("Error fetching slots:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        this.slots = [];
      } finally {
        this.loading = false;
      }
    },

    async assignBicycleToSlot(slotId: string, bicycleId: string) {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(
          `${this.baseURL}/slot/${slotId}/lock?bicycleId=${bicycleId}`,
          {
            method: "POST",
            headers: { Accept: "application/json" },
          }
        );

        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }

        await this.fetchSlots();
        return await response.text();
      } catch (error) {
        console.error("Error assigning bicycle to slot:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    },
  },
});

/*import { defineStore } from 'pinia';
import {Station} from '@/models/Station';

const stationStore = defineStore('station', {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL + '/station/stations',
  }),
  actions: {
    async fetchStations() {
      try {
        const response = await fetch(this.baseURL, {
          method: 'GET',
          headers: { Accept: 'application/json' },
        });
        if (!response.ok) {
          console.error(
            'HTTP error fetching stations:',
            response.status,
            response.statusText
          );
          return [];
        }
        const data = await response.json();
        return data as Station[];
      } catch (error) {
        console.error('Error fetching stations:', error);
        return [];
      }
    },
  },
});
*/
