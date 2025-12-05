import { defineStore } from "pinia";

export const useTravelStore = defineStore("travel", {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL + "/travel",
  }),
  actions: {
    /**
     * Inicia un viaje hacia el backend
     * @param payload - Datos del viaje (userUid, estaciones, tipo de bici)
     * @returns Respuesta del backend
     */
    async startTravel(userUid: string, stationStartId: number, stationEndId: number, bikeType: string) {
      const payload = { userUid, stationStartId, stationEndId, bikeType };
      const url = this.baseURL + "/start";
      const res = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(payload),
      });
      if (!res.ok) {
        const txt = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText} ${txt}`);
      }
      return await res.json().catch(() => null);
    },

    /**
     * Método alternativo: directamente prepara y envía el viaje
     * Se usa cuando el componente pasa todos los parámetros necesarios
     * Encapsula la lógica HTTP para que el componente no haga llamadas directas
     * @param userUid - ID del usuario en Firebase
     * @param stationStartId - ID de la estación de origen
     * @param stationEndId - ID de la estación de destino
     * @param bikeType - Tipo de bicicleta (ELECTRIC o MECHANIC)
     * @returns Respuesta del backend o null si hay error
     */
    async initiateTravel(userUid: string, stationStartId: number, stationEndId: number, bikeType: string) {
      return this.startTravel(userUid, stationStartId, stationEndId, bikeType);
    },

    /**
     * Verifica/desbloquea una bicicleta mediante código de 6 dígitos
     * Endpoint gateway: POST `${baseURL}/verify-bicycle?uid={uid}&code={code}`
     * @param userUid - UID del usuario (Firebase)
     * @param bicycleCode - Código de la bicicleta (6 dígitos)
     */
    async verifyBicycle(userUid: string, bicycleCode: string) {
      const uid = (userUid || '').trim();
      const code = (bicycleCode || '').trim();
      if (!uid) throw new Error('UID de usuario vacío');
      if (!code) throw new Error('Código de bicicleta vacío');

      const url = `${this.baseURL}/verify-bicycle?uid=${encodeURIComponent(uid)}&code=${encodeURIComponent(code)}`;
      const res = await fetch(url, {
        method: 'POST',
        headers: {
          Accept: 'application/json',
        },
      });
      if (!res.ok) {
        const txt = await res.text().catch(() => '');
        throw new Error(`HTTP ${res.status} ${res.statusText} ${txt}`);
      }
      return await res.json().catch(() => null);
    }
  },
});
