import { defineStore } from 'pinia';
import type { Station, StationDTO } from '@/models/Station';
import { toStation } from '@/models/Station';

export const useStationStore = defineStore('station', {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL + '/station',
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
        console.log('[stationStore] estaciones recibidas (raw):', data);

        const parsed: Station[] = Array.isArray(data)
          ? (data as StationDTO[]).map(toStation)
          : [];

        console.log('[stationStore] estaciones parseadas:', parsed.map(s => ({
          idStation: s.idStation,
          nameStation: s.nameStation,
          latitude: s.latitude,
          longitude: s.longitude,
        })));

        return parsed;
      } catch (error) {
        console.error('Error fetching stations:', error);
        return [];
      }
    },
  },
});

export default useStationStore;
