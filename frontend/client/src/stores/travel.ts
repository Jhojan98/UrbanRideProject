import { defineStore } from "pinia";
import type Travel from "@/models/Travel";

export const useTravelStore = defineStore("history", {
  state: () => ({
    baseURL: "/api/travel",
    travels: [] as Travel[],

  }),
  actions: {
    /**
     * Starts a trip to the backend
     * @param payload - Trip data (userUid, stations, bike type)
     * @returns Backend response
     */
    async startTravel(userUid: string, stationStartId: number, stationEndId: number, bikeType: string) {
      const payload = { userUid, stationStartId, stationEndId, bikeType };
      const url = this.baseURL + "/start";

      console.log("[TravelStore] startTravel - URL:", url);
      console.log("[TravelStore] startTravel - baseURL:", this.baseURL);
      console.log("[TravelStore] startTravel - Payload:", payload);

      const res = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(payload),
      });

      console.log("[TravelStore] startTravel - Response status:", res.status);

      if (!res.ok) {
        const txt = await res.text().catch(() => "");
        console.error("[TravelStore] startTravel - Error response:", txt);
        throw new Error(`HTTP ${res.status} ${res.statusText} ${txt}`);
      }
      return await res.json().catch(() => null);
    },

    /**
     * Alternative method: directly prepares and sends the trip
     * Used when the component passes all required parameters
     * Encapsulates HTTP logic so the component doesn't make direct calls
     * @param userUid - User ID in Firebase
     * @param stationStartId - ID of origin station
     * @param stationEndId - ID of destination station
     * @param bikeType - Bicycle type (ELECTRIC or MECHANIC)
     * @returns Backend response or null if there is an error
     */
    async initiateTravel(userUid: string, stationStartId: number, stationEndId: number, bikeType: string) {
      return this.startTravel(userUid, stationStartId, stationEndId, bikeType);
    },

    /**
     * Verifies/unlocks a bicycle via 6-digit code
     * Gateway endpoint: POST `${baseURL}/verify-bicycle?uid={uid}&code={code}`
     * @param userUid - User UID (Firebase)
     * @param bicycleCode - Bicycle code (6 digits)
     */
    async verifyBicycle(userUid: string, bicycleCode: string) {
      const uid = (userUid || '').trim();
      const code = (bicycleCode || '').trim();
      if (!uid) throw new Error('Empty user UID');
      if (!code) throw new Error('Empty bicycle code');

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
    },
    async fetchTravels(id: string) {
      try {
        const response = await fetch(`${this.baseURL}/usuario/${id}`, {
          headers: { Accept: 'application/json' }
        })
        if (!response.ok) {
          console.error('HTTP error fetching travels:', response.status, response.statusText)
          this.travels = []
          return
        }

        // Check if response has content before parsing JSON
        const text = await response.text()
        if (!text || text.trim() === '') {
          console.log('Empty response from server, no travels')
          this.travels = []
          return
        }

        try {
          const data = JSON.parse(text)
          this.travels = Array.isArray(data) ? data as Travel[] : []
        } catch (parseError) {
          console.error('Error parsing JSON travels:', parseError)
          console.log('Response received:', text)
          this.travels = []
        }
      } catch (error) {
        console.error('Error fetching travels:', error)
        this.travels = []
      }

    },

  },
});
