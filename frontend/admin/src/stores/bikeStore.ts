import { defineStore } from "pinia";
import type Bike from "@/models/Bike";
import { BicycleFactory } from "@/patterns/BicycleFlyweight";
import { bicycleWebSocketService, type BicycleLocationUpdate } from "@/services/BicycleWebSocketService";

export const useBikeStore = defineStore("bike", {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL + "/bicy",
    bikes: [] as Bike[],
    bicycleFactory: new BicycleFactory(),
    isWebSocketConnected: false,
    loading: false,
    error: null as string | null,
  }),

  getters: {
    /**
     * Obtiene todas las bicicletas
     */
    allBikes: (state) => state.bikes,

    /**
     * Alias para compatibilidad con bicycleStore
     */
    bicycles: (state) => state.bikes,

    /**
     * Obtiene el factory de bicicletas para gestión de marcadores
     */
    factory: (state) => state.bicycleFactory,

    /**
     * Verifica si el WebSocket está conectado
     */
    wsConnected: (state) => state.isWebSocketConnected,

    /**
     * Filtra bicicletas eléctricas
     */
    electricBicycles: (state) => state.bikes.filter(b => {
      const model = typeof b.model === 'string' ? b.model.toUpperCase() : b.model;
      return model === "ELECTRIC" || model === "ELÉCTRICA";
    }),

    /**
     * Filtra bicicletas mecánicas
     */
    mechanicBicycles: (state) => state.bikes.filter(b => {
      const model = typeof b.model === 'string' ? b.model.toUpperCase() : b.model;
      return model === "MECHANIC" || model === "MECÁNICA";
    }),
  },

  actions: {
    /**
     * Carga inicial de todas las bicicletas desde el backend
     */
    async fetchBikes() {
      this.loading = true;
      this.error = null;
      try {
        const url = `${this.baseURL}/`;
        const response = await fetch(url, {
          method: "GET",
          headers: { Accept: "application/json" },
        });
        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }
        const data = await response.json();

        // Usar datos directamente sin conversión DTO
        if (Array.isArray(data)) {
          this.bikes = data;
          console.log("[BikeStore] Bicicletas cargadas:", this.bikes.length);
          console.log("[BikeStore] Ejemplo de bicicleta:", this.bikes[0]);
        } else {
          this.bikes = [];
        }
      } catch (error) {
        console.error("Error fetching bikes:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        this.bikes = [];
      } finally {
        this.loading = false;
      }
    },

    /**
     * Alias para compatibilidad con bicycleStore
     */
    async fetchBicycles() {
      return this.fetchBikes();
    },

    /**
     * Conecta el WebSocket para recibir actualizaciones en tiempo real
     */
    connectWebSocket() {
      if (this.isWebSocketConnected) {
        console.log("[BikeStore] WebSocket ya está conectado");
        return;
      }

      bicycleWebSocketService.connect((bikeId: string, update: BicycleLocationUpdate) => {
        this.handleLocationUpdate(bikeId, update);
      });

      this.isWebSocketConnected = true;
      console.log("[BikeStore] WebSocket conectado");
    },

    /**
     * Maneja las actualizaciones de ubicación y batería desde el WebSocket
     */
    handleLocationUpdate(bikeId: string, update: BicycleLocationUpdate) {
      const bikeIndex = this.bikes.findIndex(b => (b.id || b.idBicycle) === bikeId);

      if (bikeIndex !== -1) {
        // Actualizar bicicleta existente con AMBOS conjuntos de campos para compatibilidad
        const updatedBike = {
          ...this.bikes[bikeIndex],
          // Campos frontend
          lat: update.latitude,
          lon: update.longitude,
          // Campos backend (mantener sincronizados)
          latitude: update.latitude,
          length: update.longitude,
          // Batería (flexible string/number)
          battery: update.battery,
          // Timestamps
          timestamp: new Date(update.timestamp),
          lastUpdate: new Date(update.timestamp),
        };

        // Reemplazar objeto completo para activar reactividad
        this.bikes.splice(bikeIndex, 1, updatedBike);

        // Actualizar también el marcador en el factory
        this.bicycleFactory.getBicycleMarker(updatedBike);

        console.log(`[BikeStore] 🔄 Bicicleta ${bikeId} actualizada vía WebSocket:`, {
          lat: update.latitude,
          lon: update.longitude,
          battery: update.battery,
        });
      } else {
        console.warn(`[BikeStore] ⚠️ Bicicleta ${bikeId} no encontrada en el store`);
      }
    },

    /**
     * Desconecta el WebSocket
     */
    disconnectWebSocket() {
      if (this.isWebSocketConnected) {
        bicycleWebSocketService.disconnect();
        this.isWebSocketConnected = false;
        console.log("[BikeStore] WebSocket desconectado");
      }
    },

    /**
     * Obtiene una bicicleta por ID
     */
    getBikeById(bikeId: string): Bike | undefined {
      return this.bikes.find(b => (b.id || b.idBicycle) === bikeId);
    },

    /**
     * Crea una bicicleta eléctrica
     */
    async createElectricBicycle(data: { series: number; padlockStatus: string; battery: number }) {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/electric`, {
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
        this.bikes.push(created);
        return created;
      } catch (error) {
        console.error("Error creating electric bicycle:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * Crea una bicicleta mecánica
     */
    async createMechanicBicycle(data: { series: number; padlockStatus: string }) {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/mechanic`, {
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
        this.bikes.push(created);
        return created;
      } catch (error) {
        console.error("Error creating mechanic bicycle:", error);
        this.error = error instanceof Error ? error.message : "Error desconocido";
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * Elimina una bicicleta por ID
     */
    async deleteBicycle(id: string) {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.baseURL}/${id}`, {
          method: "DELETE",
          headers: { Accept: "application/json" },
        });

        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }

        // Eliminar por idBicycle o id
        this.bikes = this.bikes.filter(b => (b.idBicycle || b.id) !== id);
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

export const useBicycleStore = useBikeStore;  // Alias para compatibilidad
export default useBikeStore;
