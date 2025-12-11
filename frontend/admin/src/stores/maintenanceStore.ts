import { defineStore } from 'pinia';
import type Maintenance from '@/models/Maintenance';
import { getAuth } from 'firebase/auth';

type MaintenancePayload = {
  entityType: string;
  maintenanceType: string;
  triggeredBy: string;
  description: string;
  status: string;
  date: string | Date;
  cost: number;
  bikeId: string | null;
  stationId: number | null;
  lockId: string | null;
  id?: string;
};

export const useMaintenanceStore = defineStore('maintenance', {
  state: () => ({
    baseURL: '/api/maintenance',
    maintList: [] as Maintenance[],
    loading: false as boolean,
  }),
  actions: {
    async fetchMaintenances() {
      this.loading = true;
      try {
        const auth = getAuth();
        const token = await auth.currentUser?.getIdToken();
        const response = await fetch(`${this.baseURL}/maintenance/`, {
          method: 'GET',
          headers: {
            Accept: 'application/json',
            ...(token && { Authorization: `Bearer ${token}` })
          },
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
      } finally {
        this.loading = false;
      }
    },
    async createMaintenance(payload: MaintenancePayload) {
      this.loading = true;
      try {
        const auth = getAuth();
        const token = await auth.currentUser?.getIdToken();
        const response = await fetch(`${this.baseURL}/maintenance`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
            ...(token && { Authorization: `Bearer ${token}` })
          },
          body: JSON.stringify({
            ...payload,
            date: payload.date ? new Date(payload.date) : undefined,
            bikeId: payload.bikeId ?? null,
            stationId: payload.stationId ?? null,
            lockId: payload.lockId ?? null,
          }),
        });

        if (!response.ok) {
          console.error(
            'HTTP error creating maintenance:',
            response.status,
            response.statusText
          );
          throw new Error('No se pudo crear la orden de mantenimiento');
        }

        const created = await response.json();
        this.maintList.push(created);
        return created;
      } catch (error) {
        console.error('Error creating maintenance:', error);
        throw error;
      } finally {
        this.loading = false;
      }
    },
    async updateMaintenance(id: string, payload: MaintenancePayload) {
      this.loading = true;
      try {
        const auth = getAuth();
        const token = await auth.currentUser?.getIdToken();
        const response = await fetch(`${this.baseURL}/maintenance/${id}`, {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
            ...(token && { Authorization: `Bearer ${token}` })
          },
          body: JSON.stringify({
            ...payload,
            date: payload.date ? new Date(payload.date) : undefined,
            bikeId: payload.bikeId ?? null,
            stationId: payload.stationId ?? null,
            lockId: payload.lockId ?? null,
          }),
        });

        if (!response.ok) {
          console.error(
            'HTTP error updating maintenance:',
            response.status,
            response.statusText
          );
          throw new Error('No se pudo actualizar la orden de mantenimiento');
        }

        const updated = await response.json();
        const index = this.maintList.findIndex(m => String((m as Maintenance).id ?? '') === id);
        if (index !== -1) {
          this.maintList.splice(index, 1, updated);
        } else {
          this.maintList.push(updated);
        }
        return updated;
      } catch (error) {
        console.error('Error updating maintenance:', error);
        throw error;
      } finally {
        this.loading = false;
      }
    },
  },
});

