<template>
  <div class="fines-container">
    <div class="header">
      <h1>{{ t('users.fines.title') }}</h1>
      <div class="filters">
        <select v-model="filterStatus" class="filter-select">
          <option value="">{{ t('users.fines.filterAll') }}</option>
          <option value="PENDING">{{ t('users.fines.filterPending') }}</option>
          <option value="PAID">{{ t('users.fines.filterPaid') }}</option>
          <option value="CANCELLED">{{ t('users.fines.filterCancelled') }}</option>
        </select>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">{{ t('users.fines.stats.total') }}</p>
          <p class="stat-value">{{ userStore.fines.length }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">{{ t('users.fines.stats.pending') }}</p>
          <p class="stat-value">{{ pendingFines }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">{{ t('users.fines.stats.paid') }}</p>
          <p class="stat-value">{{ paidFines }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">{{ t('users.fines.stats.totalAmount') }}</p>
          <p class="stat-value">${{ totalAmount.toFixed(2) }}</p>
        </div>
      </div>
    </div>

    <div class="table-container">
      <table class="fines-table">
        <thead>
          <tr>
            <th>{{ t('users.fines.tableHeaders.id') }}</th>
            <th>{{ t('users.fines.tableHeaders.reason') }}</th>
            <th>{{ t('users.fines.tableHeaders.description') }}</th>
            <th>{{ t('users.fines.tableHeaders.status') }}</th>
            <th>{{ t('users.fines.tableHeaders.amount') }}</th>
            <th>{{ t('users.fines.tableHeaders.date') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="fine in filteredFines"
            :key="fine.k_user_fine"
            class="fine-row"
          >
            <td>{{ fine.k_user_fine }}</td>
             <td>{{ fine.fine?.d_description }}</td>
            <td>{{ fine.n_reason  }}</td>
            <td>
              <span :class="['status-badge', `status-${(fine.t_state ?? '').toLowerCase()}`]">
                {{ fine.t_state }}
              </span>
            </td>
            <td class="amount">${{ formatAmount(fine.v_amount_snapshot) }}</td>
            <td>{{ fine.f_assigned_at ? formatDate(fine.f_assigned_at) : '' }}</td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredFines.length === 0" class="no-data">
        <p>{{ t('users.fines.noData') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import usersStore from '@/stores/userStore'

const { t } = useI18n()
const userStore = usersStore()
const filterStatus = ref('')

onMounted(async () => {
  await userStore.fetchAllFines()
})

const filteredFines = computed(() => {
  if (!filterStatus.value) {
    return userStore.fines
  }
  return userStore.fines.filter(fine => fine.t_state === filterStatus.value)
})

const pendingFines = computed(() => {
  return userStore.fines.filter(fine => fine.t_state === 'PENDING').length
})

const paidFines = computed(() => {
  return userStore.fines.filter(fine => fine.t_state === 'PAID').length
})

const totalAmount = computed(() => {
  return userStore.fines.reduce((sum, fine) => sum + (fine.v_amount_snapshot || 0), 0)
})

// Debug: log cuando cambien las multas
const _debugFines = computed(() => {
  console.log('Multas en FinesComponent:', userStore.fines)
  console.log('Multas filtradas:', filteredFines.value)
  return userStore.fines.length
})

const formatDate = (date: Date | string) => {
  return new Date(date).toLocaleString('es-ES', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatAmount = (amount: number | undefined) => {
  if (!amount) return '0.00'
  return amount.toLocaleString('es-ES', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
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

.status-pending {
  background-color: var(--color-battery-medium);
  color: var(--color-white);
}

.status-paid {
  background-color: var(--color-battery-high);
  color: var(--color-white);
}

.status-cancelled {
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
