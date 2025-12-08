import { defineStore } from "pinia";
import type { Station } from "@/models/Station";

// Interfaz extendida para datos adicionales del backend que no están en el modelo
interface StationExtended extends Station {
  type?: string;
  cctvStatus?: boolean;
  mechanical?: number;
  electric?: number;
  idCity?: number;
}

export const useStationStore = defineStore("station", {
  state: () => ({
    baseURL: process.env.VUE_APP_STATIONS_URL || "http://localhost:8005",
    stationsEndpoint: "/",
    stations: [] as StationExtended[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    allStations: (state) => state.stations,
    getStationById: (state) => (id: number) => {
      return state.stations.find((s) => s.idStation === id);
    },
  },

  actions: {
    async fetchStations(): Promise<StationExtended[]> {
      this.loading = true;
      this.error = null;

      try {
        const url = `${this.baseURL}${this.stationsEndpoint}`;
        console.log("[StationStore] Fetching from URL:", url);
        console.log("[StationStore] baseURL:", this.baseURL);
        console.log("[StationStore] stationsEndpoint:", this.stationsEndpoint);

        const res = await fetch(url, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          }
        });

        console.log("[StationStore] Response status:", res.status, res.statusText);

        if (!res.ok) {
          const errorBody = await res.text();
          console.error("[StationStore] Error body:", errorBody);
          throw new Error(`HTTP ${res.status} ${res.statusText}: ${errorBody}`);
        }

        const data = await res.json();
        console.log("[StationStore] Response data:", data);

        // Handle different response formats
        let list: Station[] = [];
        if (Array.isArray(data)) {
          list = data;
        } else if (Array.isArray(data.content)) {
          list = data.content;
        } else if (Array.isArray(data._embedded?.estaciones)) {
          list = data._embedded.estaciones;
        } else {
          // Check for any array in the response
          for (const value of Object.values(data)) {
            if (Array.isArray(value)) {
              list = value as Station[];
              break;
            }
          }
        }

        if (list.length === 0) {
          throw new Error("No stations found in response");
        }

        // Normalize station data to Station model structure
        this.stations = list.map((s: any) => ({
          idStation: s.idStation,
          nameStation: s.stationName ?? s.nameStation,
          latitude: s.latitude,
          longitude: s.length ?? s.longitude,
          totalSlots: s.totalSlots ?? 0,
          availableSlots: s.availableSlots ?? 0,
          timestamp: s.timestamp ? new Date(s.timestamp) : new Date(),
          slots: s.slots,
          // Campos extendidos
          type: s.type,
          cctvStatus: typeof s.cctvStatus === 'boolean' ? s.cctvStatus : s.cctvStatus === 'true',
          mechanical: s.mechanical ?? 0,
          electric: s.electric ?? 0,
          idCity: s.idCity
        }));

        // Logging para verificación
        console.log('[StationStore] Estaciones normalizadas:');
        this.stations.slice(0, 3).forEach(st => {
          console.log(`  - ${st.nameStation}: ⚡ ${st.electric}, ⚙️ ${st.mechanical}`);
        });

        this.loading = false;
        console.log(
          `[StationStore] Successfully loaded ${this.stations.length} stations`
        );
        return this.stations;
      } catch (err) {
        this.error = `Error al cargar estaciones: ${err}`;
        this.loading = false;
        console.error("[StationStore]", this.error);
        return [];
      }
    },

    async getStationDetails(id: number): Promise<StationExtended | null> {
      try {
        const url = `${this.baseURL}${this.stationsEndpoint}/${id}`;
        const res = await fetch(url);

        if (!res.ok) {
          throw new Error(`HTTP ${res.status}`);
        }

        const data = await res.json();
        return {
          idStation: data.idStation,
          nameStation: data.stationName ?? data.nameStation,
          latitude: data.latitude,
          longitude: data.length ?? data.longitude,
          totalSlots: data.totalSlots ?? 0,
          availableSlots: data.availableSlots ?? 0,
          timestamp: data.timestamp ? new Date(data.timestamp) : new Date(),
          slots: data.slots,
          // Campos extendidos
          type: data.type,
          cctvStatus: typeof data.cctvStatus === 'boolean' ? data.cctvStatus : data.cctvStatus === 'true',
          mechanical: data.mechanical ?? 0,
          electric: data.electric ?? 0,
          idCity: data.idCity
        };
      } catch (err) {
        console.error(`[StationStore] Error fetching station ${id}:`, err);
        return null;
      }
    },

    clearStations() {
      this.stations = [];
      this.error = null;
    },
  },
});
