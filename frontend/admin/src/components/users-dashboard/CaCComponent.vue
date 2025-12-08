<template>
  <div class="cac-container">
    <div class="header">
      <h1>{{ t('users.cac.title') }}</h1>
      <div class="filters">
        <select v-model="filterStatus" class="filter-select">
          <option value="">{{ t('users.cac.filterAll') }}</option>
          <option value="PENDIENTE">{{ t('users.cac.filterPending') }}</option>
          <option value="EN_PROCESO">{{ t('users.cac.filterInProcess') }}</option>
          <option value="RESUELTO">{{ t('users.cac.filterResolved') }}</option>
          <option value="CERRADO">{{ t('users.cac.filterClosed') }}</option>
        </select>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">{{ t('users.cac.stats.total') }}</p>
          <p class="stat-value">{{ userStore.cacs.length }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">{{ t('users.cac.stats.pending') }}</p>
          <p class="stat-value">{{ pendingCaCs }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">{{ t('users.cac.stats.inProcess') }}</p>
          <p class="stat-value">{{ inProcessCaCs }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <p class="stat-label">{{ t('users.cac.stats.resolved') }}</p>
          <p class="stat-value">{{ resolvedCaCs }}</p>
        </div>
      </div>
    </div>

    <div class="cacs-grid">
      <div
        v-for="cac in filteredCaCs"
        :key="cac.idCaC"
        class="cac-card"
        @click="selectCaC(cac)"
      >
        <div class="cac-header">
          <span class="cac-id">{{ t('users.cac.ticketId') }} #{{ cac.idCaC }}</span>
          <span :class="['status-badge', `status-${cac.status.toLowerCase()}`]">
            {{ cac.status }}
          </span>
        </div>

        <div class="cac-body">
          <p class="cac-description">{{ cac.description }}</p>
        </div>

        <div class="cac-footer">
          <div class="cac-meta">
            <span class="meta-item">
              {{ t('users.cac.travelId') }} #{{ cac.idTravel }}
            </span>
            <span class="meta-item">
              {{ formatDate(cac.date) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="filteredCaCs.length === 0" class="no-data">
      <p>{{ t('users.cac.noData') }}</p>
    </div>

    <!-- Modal de detalle -->
    <div v-if="selectedCaC" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ t('users.cac.modalTitle') }} #{{ selectedCaC.idCaC }}</h2>
          <button @click="closeModal" class="btn-close">✕</button>
        </div>

        <div class="modal-body">
          <div class="detail-section">
            <h3>{{ t('users.cac.modalSections.status') }}</h3>
            <span :class="['status-badge-large', `status-${selectedCaC.status.toLowerCase()}`]">
              {{ selectedCaC.status }}
            </span>
          </div>

          <div class="detail-section">
            <h3>{{ t('users.cac.modalSections.description') }}</h3>
            <p class="description-text">{{ selectedCaC.description }}</p>
          </div>

          <div class="detail-section">
            <h3>{{ t('users.cac.modalSections.travelInfo') }}</h3>
            <p><strong>{{ t('users.cac.modalSections.travelIdLabel') }}:</strong> {{ selectedCaC.idTravel }}</p>
          </div>

          <div class="detail-section">
            <h3>{{ t('users.cac.modalSections.date') }}</h3>
            <p>{{ formatDateTime(selectedCaC.date) }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import usersStore from '@/stores/userStore'
import type CaC from '@/models/CaC'

const { t } = useI18n()
const userStore = usersStore()
const filterStatus = ref('')
const selectedCaC: Ref<CaC | null> = ref(null)

const filteredCaCs = computed(() => {
  if (!filterStatus.value) {
    return userStore.cacs
  }
  return userStore.cacs.filter(cac => cac.status === filterStatus.value)
})

const pendingCaCs = computed(() => {
  return userStore.cacs.filter(cac => cac.status === 'PENDIENTE').length
})

const inProcessCaCs = computed(() => {
  return userStore.cacs.filter(cac => cac.status === 'EN_PROCESO').length
})

const resolvedCaCs = computed(() => {
  return userStore.cacs.filter(cac => cac.status === 'RESUELTO' || cac.status === 'CERRADO').length
})

const selectCaC = (cac: CaC) => {
  selectedCaC.value = cac
}

const closeModal = () => {
  selectedCaC.value = null
}

const formatDate = (date: Date | string | number) => {
  return new Date(date).toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const formatDateTime = (date: Date | string | number) => {
  return new Date(date).toLocaleString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.cac-container {
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

.cacs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.cac-card {
  background: var(--color-background-light);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 4px solid var(--color-gray-medium);
}

.cac-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.cac-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--color-border-light);
}

.cac-id {
  font-weight: 700;
  color: var(--color-text-primary-light);
  font-size: 1rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge-large {
  padding: 0.5rem 1.5rem;
  border-radius: 20px;
  font-size: 1rem;
  font-weight: 600;
  text-transform: uppercase;
  display: inline-block;
}

.status-pendiente {
  background-color: var(--color-battery-medium);
  color: var(--color-white);
}

.status-en_proceso {
  background-color: var(--color-iot-good);
  color: var(--color-white);
}

.status-resuelto {
  background-color: var(--color-battery-high);
  color: var(--color-white);
}

.status-cerrado {
  background-color: var(--color-iot-loss);
  color: var(--color-white);
}

.cac-body {
  margin-bottom: 1rem;
}

.cac-description {
  color: var(--color-text-secondary-light);
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cac-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cac-meta {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--color-gray-medium);
  font-size: 0.875rem;
}

.no-data {
  padding: 3rem;
  text-align: center;
  color: var(--color-gray-medium);
  font-size: 1.125rem;
  background: var(--color-background-light);
  border-radius: 12px;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: var(--color-background-light);
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid var(--color-border-light);
  background: var(--color-primary-light);
  color: var(--color-white);
  border-radius: 16px 16px 0 0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.btn-close {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.btn-close:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.modal-body {
  padding: 2rem;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 1.5rem;
}

.detail-section h3 {
  margin: 0 0 0.75rem 0;
  color: var(--color-text-primary-light);
  font-size: 1.125rem;
  font-weight: 600;
}

.detail-section p {
  margin: 0;
  color: var(--color-text-secondary-light);
  line-height: 1.6;
}

.description-text {
  background: var(--color-gray-very-light);
  padding: 1rem;
  border-radius: var(--border-radius);
  border-left: 4px solid var(--color-gray-medium);
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

  .cacs-grid {
    grid-template-columns: 1fr;
  }

  .modal-content {
    width: 95%;
    max-height: 90vh;
  }

  .modal-header {
    padding: 1rem;
  }

  .modal-body {
    padding: 1rem;
  }
}

html[data-theme="dark"] .stat-card,
html[data-theme="dark"] .cac-card,
html[data-theme="dark"] .no-data,
html[data-theme="dark"] .modal-content {
  background: var(--color-surface-dark);
}

html[data-theme="dark"] .cac-description,
html[data-theme="dark"] .detail-section p {
  color: var(--color-text-secondary-dark);
}

html[data-theme="dark"] .description-text {
  background: var(--color-gray-light);
}
</style>
