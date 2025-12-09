import { defineStore } from "pinia";
import type { Station } from "@/models/Station";

// Extended interface for additional backend data not in the model
interface StationExtended extends Station {
  type?: string;
  cctvStatus?: boolean;
  mechanical?: number;
  electric?: number;
  idCity?: number;
  availableElectricBikes?: number;
  availableMechanicBikes?: number;
}

type RawStation = Partial<StationExtended> & Record<string, unknown>;

export const useStationStore = defineStore("station", {
  state: () => ({
    baseURL: "/api",
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
        let list: Array<Station | RawStation> = [];
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
        this.stations = list.map((s) => {
          const raw = s as RawStation;
          const mechanical = raw.mechanical
            ?? raw.availableMechanicBikes
            ?? (raw as Record<string, unknown>).availableMechanic as number | undefined
            ?? (raw as Record<string, unknown>).bikesMechanical as number | undefined
            ?? raw.mechanic
            ?? 0;

          const electric = raw.electric
            ?? raw.availableElectricBikes
            ?? (raw as Record<string, unknown>).availableElectric as number | undefined
            ?? (raw as Record<string, unknown>).bikesElectric as number | undefined
            ?? (raw as Record<string, unknown>).electricBikes as number | undefined
            ?? 0;

          return {
            idStation: raw.idStation,
            nameStation: (raw as Record<string, unknown>).stationName as string ?? raw.nameStation,
            latitude: raw.latitude as number,
            longitude: (raw as Record<string, unknown>).length as number ?? raw.longitude,
            totalSlots: raw.totalSlots ?? 0,
            availableSlots: raw.availableSlots ?? 0,
            timestamp: typeof raw.timestamp === 'number' || typeof raw.timestamp === 'string'
              ? new Date(raw.timestamp)
              : raw.timestamp instanceof Date
                ? raw.timestamp
                : new Date(),
            slots: raw.slots as StationExtended['slots'],
            // Extended fields
            type: raw.type,
            cctvStatus: typeof raw.cctvStatus === 'boolean' ? raw.cctvStatus : raw.cctvStatus === 'true',
            mechanical,
            electric,
          availableElectricBikes: raw.availableElectricBikes ?? electric,
          availableMechanicBikes: raw.availableMechanicBikes ?? mechanical,
            idCity: raw.idCity
          } as StationExtended;
        });

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

    /** Apply live telemetry to the store so UI (dropdowns/reservation) stays in sync with WS */
    updateStationTelemetry(id: number, payload: {
      availableElectricBikes?: number;
      availableMechanicBikes?: number;
      timestamp?: number;
    }) {
      const idx = this.stations.findIndex(st => st.idStation === id);
      if (idx === -1) return;

      const current = this.stations[idx];
      const mechanical = payload.availableMechanicBikes ?? current.availableMechanicBikes ?? current.mechanical ?? 0;
      const electric = payload.availableElectricBikes ?? current.availableElectricBikes ?? current.electric ?? 0;
      const availableSlots = mechanical + electric;

      // Replace object to keep reactivity
      this.stations[idx] = {
        ...current,
        mechanical,
        electric,
        availableMechanicBikes: payload.availableMechanicBikes ?? current.availableMechanicBikes,
        availableElectricBikes: payload.availableElectricBikes ?? current.availableElectricBikes,
        availableSlots,
        timestamp: payload.timestamp ? new Date(payload.timestamp) : current.timestamp
      } as StationExtended;
    }
  },
});
