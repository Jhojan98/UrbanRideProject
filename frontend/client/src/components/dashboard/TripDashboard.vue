<template>
  <section class="dashboard-card">
    <header class="dashboard-header">
      <h3>{{ $t('profile.trips.title') }}</h3>
      <span v-if="loading" class="badge muted">{{ $t('profile.loading') }}</span>
    </header>

    <div v-if="loading" class="loading-message">{{ $t('profile.loading') }}</div>
    <div v-else-if="travels.length === 0" class="empty-message">{{ $t('profile.trips.noTrips') }}</div>
    <div v-else class="table-container">
      <table class="trips-table">
        <thead>
          <tr>
            <th>{{ $t('profile.trips.startStation') }}</th>
            <th>{{ $t('profile.trips.endStation') }}</th>
            <th>{{ $t('profile.trips.date') }}</th>
            <th>{{ $t('profile.trips.status') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="trip in travels" :key="trip.idTravel">
            <td>{{ trip.fromIdStation }}</td>
            <td>{{ trip.toIdStation || 'N/A' }}</td>
            <td>{{ formatDate(trip.startedAt) }}</td>
            <td>{{ trip.status }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script lang="ts">
import type { PropType } from 'vue';
import type Travel from '@/models/Travel';

export default {
  name: 'TripDashboard',
  props: {
    travels: {
      type: Array as PropType<Travel[]>,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    formatDate(date: string | number | Date | undefined): string {
      if (!date) return 'N/A';
      const dateObj = typeof date === 'string' || typeof date === 'number' ? new Date(date) : date;
      return dateObj.toLocaleDateString('es-CO', { year: 'numeric', month: 'short', day: 'numeric' });
    }
  }
};
</script>

<style scoped>
.dashboard-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.dashboard-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.badge.muted { color: #666; font-size: 13px; }

.loading-message, .empty-message { padding: 10px; color: #555; }

.table-container { overflow-x: auto; }

.trips-table { width: 100%; border-collapse: collapse; }
.trips-table th, .trips-table td { padding: 10px; border-bottom: 1px solid #eee; text-align: left; }
.trips-table th { background: #f7f9fc; font-weight: 600; font-size: 14px; }
.trips-table td { font-size: 13px; color: #333; }

@media (max-width: 768px) {
  .dashboard-card { padding: 12px; }
  .trips-table th, .trips-table td { padding: 8px 6px; font-size: 12px; }
}
</style>
