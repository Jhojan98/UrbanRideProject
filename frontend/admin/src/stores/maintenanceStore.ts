import { defineStore } from 'pinia';
import type Maintenance from '@/models/Maintenance';

export const useMaintenanceStore = defineStore('maintenance', {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL + '/maintenance',
    maintList: [] as Maintenance[],
  }),
  actions: {
    async fetchMaintenances() {
      try {
        const response = await fetch(`${this.baseURL}/maintenance/`, {
          method: 'GET',
          headers: { Accept: 'application/json' },
        });
        if (!response.ok) {
          console.error(
            'HTTP error fetching maintenances:',
            response.status,
            response.statusText
          );
          this.maintList = [];
          return;
        }
        const data = await response.json();
        this.maintList = Array.isArray(data) ? data : [];
      } catch (error) {
        console.error('Error fetching maintenances:', error);
        this.maintList = [];
      }
    },
    async createMaintenance(entityType: string, maintenanceType: string, triggeredBy: string, description: string, status: string, date: Date, cost: number, bikeId: string, stationId: number, lockId: string, id: string) {
      try {
        const response = await fetch(`${this.baseURL}/maintenance/`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
          },
          body: JSON.stringify({
            entityType,
            maintenanceType,
            triggeredBy,
            description,
            status,
            date,
            cost,
            bikeId,
            stationId,
            lockId,
            id
          }),
        });

        if (!response.ok) {
          console.error(
            'HTTP error creating maintenance:',
            response.status,
            response.statusText
          );
          return null;
        }

        const created = await response.json();
        this.maintList.push(created);
        return created;
      } catch (error) {
        console.error('Error creating maintenance:', error);
        return null;
      }
    },
  },
});

