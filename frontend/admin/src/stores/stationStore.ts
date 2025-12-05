import { defineStore } from 'pinia';
import Station from '@/models/Station';

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
