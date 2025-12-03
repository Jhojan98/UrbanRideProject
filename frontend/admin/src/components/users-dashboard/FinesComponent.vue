<template>
  <div class="fines-container">
    <div class="header">
      <h1>Gestión de Multas</h1>
      <div class="filters">
        <select v-model="filterStatus" class="filter-select">
          <option value="">Todos los estados</option>
          <option value="PENDIENTE">Pendiente</option>
          <option value="PAGADA">Pagada</option>
          <option value="CANCELADA">Cancelada</option>
        </select>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">Total Multas</p>
          <p class="stat-value">{{ userStore.fines.length }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">Pendientes</p>
          <p class="stat-value">{{ pendingFines }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">Pagadas</p>
          <p class="stat-value">{{ paidFines }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">Monto Total</p>
          <p class="stat-value">${{ totalAmount.toFixed(2) }}</p>
        </div>
      </div>
    </div>

    <div class="table-container">
      <table class="fines-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Razón</th>
            <th>Estado</th>
            <th>Monto</th>
            <th>Fecha</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="fine in filteredFines"
            :key="fine.idFine"
            class="fine-row"
          >
            <td>{{ fine.idFine }}</td>
            <td>{{ fine.reason }}</td>
            <td>
              <span :class="['status-badge', `status-${fine.state.toLowerCase()}`]">
                {{ fine.state }}
              </span>
            </td>
            <td class="amount">${{ fine.amount.toFixed(2) }}</td>
            <td>{{ formatDate(fine.timestamp) }}</td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredFines.length === 0" class="no-data">
        <p>No se encontraron multas</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import usersStore from '@/stores/usersStore'

const userStore = usersStore()
const filterStatus = ref('')

const filteredFines = computed(() => {
  if (!filterStatus.value) {
    return userStore.fines
  }
  return userStore.fines.filter(fine => fine.state === filterStatus.value)
})

const pendingFines = computed(() => {
  return userStore.fines.filter(fine => fine.state === 'PENDIENTE').length
})

const paidFines = computed(() => {
  return userStore.fines.filter(fine => fine.state === 'PAGADA').length
})

const totalAmount = computed(() => {
  return userStore.fines.reduce((sum, fine) => sum + fine.amount, 0)
})

const formatDate = (date: Date) => {
  return new Date(date).toLocaleString('es-ES', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.fines-container {
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header h1 {
  margin: 0;
  color: var(--color-text-primary-light);
  font-size: 2rem;
}

.filters {
  display: flex;
  gap: 1rem;
}

.filter-select {
  padding: 0.75rem 1rem;
  border: 2px solid var(--color-border-light);
  border-radius: var(--border-radius);
  font-size: 1rem;
  background: var(--color-background-light);
  cursor: pointer;
  transition: border-color 0.3s;
}

.filter-select:focus {
  outline: none;
  border-color: var(--color-green-main);
  box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.1);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: var(--color-background-light);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border-left: 4px solid var(--color-gray-medium);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-info {
  flex: 1;
}

.stat-label {
  margin: 0;
  color: var(--color-gray-medium);
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  margin: 0.5rem 0 0 0;
  color: var(--color-text-primary-light);
  font-size: 1.75rem;
  font-weight: 700;
}

.table-container {
  background: var(--color-background-light);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.fines-table {
  width: 100%;
  border-collapse: collapse;
}

.fines-table thead {
  background: var(--color-primary-light);
  color: var(--color-white);
}

.fines-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.875rem;
  letter-spacing: 0.5px;
}

.fines-table tbody tr {
  border-bottom: 1px solid var(--color-border-light);
  transition: background-color 0.3s ease;
}

.fine-row:hover {
  background-color: var(--color-gray-very-light);
}

.fines-table td {
  padding: 1rem;
  color: var(--color-text-secondary-light);
}

.amount {
  font-weight: 700;
  color: var(--color-red-alert);
  font-size: 1.125rem;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-pendiente {
  background-color: var(--color-battery-medium);
  color: var(--color-white);
}

.status-pagada {
  background-color: var(--color-battery-high);
  color: var(--color-white);
}

.status-cancelada {
  background-color: var(--color-battery-low);
  color: var(--color-white);
}

.no-data {
  padding: 3rem;
  text-align: center;
  color: var(--color-gray-medium);
  font-size: 1.125rem;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .fines-table {
    font-size: 0.875rem;
  }

  .fines-table th,
  .fines-table td {
    padding: 0.75rem 0.5rem;
  }
}

html[data-theme="dark"] .stat-card,
html[data-theme="dark"] .table-container {
  background: var(--color-surface-dark);
}

html[data-theme="dark"] .fines-table td {
  color: var(--color-text-primary-dark);
}
</style>