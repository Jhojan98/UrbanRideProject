import { defineStore } from "pinia";
import type Bike from "@/models/Bike";
import { type BikeDTO, toBike } from "@/models/Bike";
import { BicycleFactory } from "@/patterns/BicycleFlyweight";
import { bicycleWebSocketService, type BicycleLocationUpdate } from "@/services/BicycleWebSocketService";

const bikeStore = defineStore("bike", {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL + "/bicy",
    bikes: [] as Bike[],
    bicycleFactory: new BicycleFactory(),
    isWebSocketConnected: false,
  }),
  actions: {
    /**
     * Carga inicial de todas las bicicletas desde el backend
     */
    async fetchBikes() {
      try {
        const url = `${this.baseURL}/`;
        const response = await fetch(url, {
          method: "GET",
          headers: { Accept: "application/json" },
        });
        if (!response.ok) {
          console.error(
            "HTTP error fetching bikes:",
            response.status,
            response.statusText
          );
          this.bikes = [];
          return;
        }
        const data = await response.json();

        // Convertir DTOs a modelo Bike
        if (Array.isArray(data)) {
          this.bikes = data.map((dto: BikeDTO) => toBike(dto));
          console.log("[BikeStore] Bicicletas cargadas:", this.bikes.length);
          console.log("[BikeStore] Ejemplo de bicicleta:", this.bikes[0]);
        } else {
          this.bikes = [];
        }
      } catch (error) {
        console.error("Error fetching bikes:", error);
        this.bikes = [];
      }
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
      const bikeIndex = this.bikes.findIndex(b => b.id === bikeId);

      if (bikeIndex !== -1) {
        // Actualizar bicicleta existente
        const updatedBike = {
          ...this.bikes[bikeIndex],
          lat: update.latitude,
          lon: update.longitude,
          battery: update.battery.toString(),
          timestamp: new Date(update.timestamp),
        };

        this.bikes[bikeIndex] = updatedBike;

        console.log(`[BikeStore] Bicicleta ${bikeId} actualizada:`, {
          lat: update.latitude,
          lon: update.longitude,
          battery: update.battery,
        });
      } else {
        console.warn(`[BikeStore] Bicicleta ${bikeId} no encontrada en el store`);
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
      return this.bikes.find(b => b.id === bikeId);
    },
  },
  getters: {
    /**
     * Obtiene todas las bicicletas
     */
    allBikes: (state) => state.bikes,

    /**
     * Obtiene el factory de bicicletas para gestión de marcadores
     */
    factory: (state) => state.bicycleFactory,

    /**
     * Verifica si el WebSocket está conectado
     */
    wsConnected: (state) => state.isWebSocketConnected,
  },
});

export const useBikeStore = bikeStore;
export default bikeStore;
