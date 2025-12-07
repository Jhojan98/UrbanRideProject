import { defineStore } from "pinia";
import type { Station } from "@/models/Station";

// Extended interface for additional backend data not in the model
interface StationExtended extends Station {
  type?: string;
  cctvStatus?: boolean;
  mechanical?: number;
  electric?: number;
  idCity?: number;
}

export const useStationStore = defineStore("station", {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL || "http://localhost:8090",
    stationsEndpoint: "/station",
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

        const res = await fetch(url);

        if (!res.ok) {
          throw new Error(`HTTP ${res.status} ${res.statusText}`);
        }

        const data = await res.json();

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
          // Extended fields
          type: s.type,
          cctvStatus: typeof s.cctvStatus === 'boolean' ? s.cctvStatus : s.cctvStatus === 'true',
          mechanical: s.mechanical ?? 0,
          electric: s.electric ?? 0,
          idCity: s.idCity
        }));

        // Logging for verification
        console.log('[StationStore] Normalized stations:');
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
          // Extended fields
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
