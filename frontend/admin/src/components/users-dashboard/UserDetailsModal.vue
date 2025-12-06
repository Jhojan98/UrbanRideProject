<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>Información del Usuario - {{ user?.userName }}</h2>
        <button @click="$emit('close')" class="btn-close">✕</button>
      </div>

      <div class="modal-body">
        <div class="tabs">
          <button :class="['tab', {active: activeTab === 'travels'}]" @click="activeTab = 'travels'">Viajes ({{ travels.length }})</button>
          <button :class="['tab', {active: activeTab === 'fines'}]" @click="activeTab = 'fines'">Multas ({{ fines.length }})</button>
          <button :class="['tab', {active: activeTab === 'cacs'}]" @click="activeTab = 'cacs'">Quejas/Comentarios ({{ cacs.length }})</button>
        </div>

        <div v-if="loading" class="loading">Cargando información...</div>

        <!-- Viajes -->
        <div v-if="activeTab === 'travels'">
          <div v-if="travels.length === 0" class="no-data">
            <p>Este usuario no tiene viajes registrados</p>
          </div>
          <div v-else class="travels-list">
            <div v-for="travel in travels" :key="travel.idTravel" class="travel-card">
              <div class="travel-header">
                <span class="travel-id">Viaje #{{ travel.idTravel }}</span>
                <span :class="['status-badge', `status-${(travel.status ?? '').toLowerCase()}`]">{{ travel.status }}</span>
              </div>
              <div class="travel-details">
                <div class="detail-row"><span class="label">Tipo:</span><span class="value">{{ travel.travelType }}</span></div>
                <div class="detail-row"><span class="label">Bicicleta:</span><span class="value">{{ (travel as any).idBicycle ?? '' }}</span></div>
                <div class="detail-row"><span class="label">Estación de Inicio:</span><span class="value">{{ travel.fromIdStation }}</span></div>
                <div class="detail-row"><span class="label">Estación de Fin:</span><span class="value">{{ travel.toIdStation ?? '' }}</span></div>
                <div class="detail-row"><span class="label">Inicio:</span><span class="value">{{ travel.startedAt ? formatDateTime(travel.startedAt as any) : '' }}</span></div>
                <div class="detail-row" v-if="travel.endedAt"><span class="label">Fin:</span><span class="value">{{ formatDateTime(travel.endedAt as any) }}</span></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Multas -->
        <div v-if="activeTab === 'fines'">
          <div v-if="fines.length === 0" class="no-data">
            <p>Este usuario no registra multas</p>
          </div>
          <div v-else class="fines-list">
            <div v-for="fine in fines" :key="fine.k_user_fine ?? fine.idFine" class="fine-card">
              <div class="fine-header">
                <span class="fine-id">Multa #{{ fine.k_user_fine ?? fine.idFine }}</span>
                <span :class="['status-badge', `status-${(fine.t_state ?? fine.state ?? '').toLowerCase()}`]">{{ fine.t_state ?? fine.state }}</span>
              </div>
              <div class="fine-details">
                <div class="detail-row"><span class="label">Razón:</span><span class="value">{{ fine.n_reason ?? fine.fine?.d_description ?? fine.reason ?? '' }}</span></div>
                <div class="detail-row"><span class="label">Monto:</span><span class="value">${{ (fine.v_amount_snapshot ?? fine.amount ?? 0).toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</span></div>
                <div class="detail-row"><span class="label">Fecha:</span><span class="value">{{ (fine.f_assigned_at ?? fine.timestamp) ? formatDateTime(fine.f_assigned_at as any ?? fine.timestamp as any) : '' }}</span></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Quejas y Comentarios -->
        <div v-if="activeTab === 'cacs'">
          <div v-if="cacs.length === 0" class="no-data">
            <p>Este usuario no registra quejas o comentarios</p>
          </div>
          <div v-else class="cacs-list">
            <div v-for="cac in cacs" :key="cac.idCaC" class="cac-card">
              <div class="cac-header">
                <span class="cac-id">Ticket #{{ cac.idCaC }}</span>
                <span :class="['status-badge', `status-${(cac.status ?? '').toLowerCase()}`]">{{ cac.status }}</span>
              </div>
              <div class="cac-details">
                <div class="detail-row"><span class="label">Descripción:</span><span class="value">{{ cac.description }}</span></div>
                <div class="detail-row"><span class="label">Viaje:</span><span class="value">#{{ cac.idTravel }}</span></div>
                <div class="detail-row"><span class="label">Fecha:</span><span class="value">{{ cac.date ? formatDate(cac.date as Date) : '' }}</span></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits } from 'vue'
import type User from '@/models/User'
import type Travel from '@/models/Travel'
import type Fine from '@/models/Fine'
import type CaC from '@/models/CaC'

defineProps<{
  user: User | null
  travels: Travel[]
  fines: Fine[]
  cacs: CaC[]
  loading: boolean
}>()

defineEmits<{
  close: []
}>()

const activeTab = ref<'travels' | 'fines' | 'cacs'>('travels')

const formatDate = (date: Date) => {
  return new Date(date).toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatDateTime = (date: Date | string) => {
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
  max-width: 800px;
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

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tab {
  padding: 0.5rem 0.9rem;
  border-radius: 999px;
  border: 1px solid var(--color-border-light);
  background: var(--color-background-light);
  color: var(--color-text-primary-light);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab.active {
  background: var(--color-primary-light);
  color: var(--color-white);
  border-color: var(--color-primary-light);
}

.fines-list,
.cacs-list,
.travels-list {
  display: grid;
  gap: 1rem;
}

.fine-card,
.cac-card,
.travel-card {
  background: var(--color-gray-very-light);
  border-radius: 12px;
  padding: 1.2rem;
  border-left: 4px solid var(--color-gray-medium);
  transition: all 0.3s ease;
}

.fine-card:hover,
.cac-card:hover,
.travel-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateX(4px);
}

.fine-header,
.cac-header,
.travel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  border-bottom: 1px solid var(--color-border-light);
  padding-bottom: 0.5rem;
}

.travel-id,
.fine-id,
.cac-id {
  font-weight: 700;
  color: var(--color-text-primary-light);
  font-size: 1.125rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
  font-size: 1.125rem;
}

.no-data {
  padding: 3rem;
  text-align: center;
  color: #999;
  font-size: 1.125rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-completed {
  background-color: #d4edda;
  color: #155724;
}

.status-active,
.status-en_curso {
  background-color: #fff3cd;
  color: #856404;
}

.status-cancelled,
.status-cancelado {
  background-color: #f8d7da;
  color: #721c24;
}

.status-pending {
  background-color: var(--color-battery-medium);
  color: var(--color-white);
}

.status-paid {
  background-color: var(--color-battery-high);
  color: var(--color-white);
}

.travel-details,
.fine-details,
.cac-details {
  display: grid;
  gap: 0.75rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-row .label {
  font-weight: 600;
  color: var(--color-gray-medium);
}

.detail-row .value {
  color: var(--color-text-primary-light);
  font-weight: 500;
}

@media (max-width: 768px) {
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
</style>
