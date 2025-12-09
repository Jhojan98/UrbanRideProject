<template>
  <div class="section">
    <div class="section-header">
      <h2>{{ t('management.complaints.title') }}</h2>
      <div class="filters">
        <select v-model="filterStatus" class="filter-select">
          <option value="all">{{ t('management.complaints.allStatus') }}</option>
          <option value="OPEN">{{ t('management.complaints.status.open') }}</option>
          <option value="IN_PROGRESS">{{ t('management.complaints.status.inProgress') }}</option>
          <option value="RESOLVED">{{ t('management.complaints.status.resolved') }}</option>
          <option value="CLOSED">{{ t('management.complaints.status.closed') }}</option>
        </select>
        <button class="btn-primary btn-sm" @click="complaintsStore.fetchComplaints()">
          <span class="material-symbols-outlined">refresh</span>
          {{ t('common.refresh') }}
        </button>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card open">
        <span class="material-symbols-outlined">report_problem</span>
        <div class="stat-info">
          <span class="stat-value">{{ complaintsStore.openComplaints.length }}</span>
          <span class="stat-label">{{ t('management.complaints.status.open') }}</span>
        </div>
      </div>
      <div class="stat-card in-progress">
        <span class="material-symbols-outlined">pending</span>
        <div class="stat-info">
          <span class="stat-value">{{ complaintsStore.inProgressComplaints.length }}</span>
          <span class="stat-label">{{ t('management.complaints.status.inProgress') }}</span>
        </div>
      </div>
      <div class="stat-card resolved">
        <span class="material-symbols-outlined">check_circle</span>
        <div class="stat-info">
          <span class="stat-value">{{ complaintsStore.resolvedComplaints.length }}</span>
          <span class="stat-label">{{ t('management.complaints.status.resolved') }}</span>
        </div>
      </div>
      <div class="stat-card closed">
        <span class="material-symbols-outlined">cancel</span>
        <div class="stat-info">
          <span class="stat-value">{{ complaintsStore.closedComplaints.length }}</span>
          <span class="stat-label">{{ t('management.complaints.status.closed') }}</span>
        </div>
      </div>
    </div>

    <!-- Loading indicator -->
    <div v-if="complaintsStore.loading" class="loading-state">
      <div class="spinner"></div>
      <p>{{ t('common.loading') }}</p>
    </div>

    <!-- Error state -->
    <div v-else-if="complaintsStore.error" class="error-state">
      <span class="material-symbols-outlined">error</span>
      <p>{{ complaintsStore.error }}</p>
      <button class="btn-primary btn-sm" @click="complaintsStore.fetchComplaints()">
        {{ t('common.refresh') }}
      </button>
    </div>

    <div v-else class="data-table">
      <table v-if="filteredComplaints.length">
        <thead>
          <tr>
            <th>{{ t('common.id') }}</th>
            <th>{{ t('management.complaints.type') }}</th>
            <th>{{ t('management.complaints.description') }}</th>
            <th>{{ t('management.complaints.travelId') }}</th>
            <th>{{ t('management.complaints.status.label') }}</th>
            <th>{{ t('management.complaints.date') }}</th>
            <th>{{ t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="complaint in filteredComplaints" :key="complaint.k_id_complaints_and_claims">
            <td>{{ complaint.k_id_complaints_and_claims }}</td>
            <td>
              <span :class="['type-badge', getTypeClass(complaint.t_type)]">
                {{ getTypeLabel(complaint.t_type) }}
              </span>
            </td>
            <td class="description-cell">
              <div class="description-text">{{ complaint.d_description }}</div>
            </td>
            <td>{{ complaint.k_id_travel }}</td>
            <td>
              <select
                :value="complaint.t_status"
                :class="['status-select', getStatusClass(complaint.t_status)]"
                @change="handleStatusChange(complaint.k_id_complaints_and_claims, $event)"
              >
                <option value="OPEN">{{ t('management.complaints.status.open') }}</option>
                <option value="IN_PROGRESS">{{ t('management.complaints.status.inProgress') }}</option>
                <option value="RESOLVED">{{ t('management.complaints.status.resolved') }}</option>
                <option value="CLOSED">{{ t('management.complaints.status.closed') }}</option>
              </select>
            </td>
            <td>{{ formatDate(complaint.created_at) }}</td>
            <td>
              <div class="action-buttons">
                <button
                  class="btn-info btn-sm"
                  :title="t('common.view')"
                  @click="viewComplaint(complaint)"
                >
                  <span class="material-symbols-outlined">visibility</span>
                </button>
                <button
                  class="btn-danger btn-sm"
                  :title="t('common.delete')"
                  @click="handleDelete(complaint.k_id_complaints_and_claims)"
                >
                  <span class="material-symbols-outlined">delete</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty-message">
        {{ filterStatus === 'all'
          ? t('management.complaints.empty')
          : t('management.complaints.emptyFilter')
        }}
      </p>
    </div>

    <!-- Modal de detalles -->
    <div v-if="selectedComplaint" class="modal-overlay" @click.self="selectedComplaint = null">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ t('management.complaints.details') }}</h3>
          <button class="btn-close" @click="selectedComplaint = null">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        <div class="modal-body">
          <div class="detail-row">
            <strong>{{ t('common.id') }}:</strong>
            <span>{{ selectedComplaint.k_id_complaints_and_claims }}</span>
          </div>
          <div class="detail-row">
            <strong>{{ t('management.complaints.type') }}:</strong>
            <span :class="['type-badge', getTypeClass(selectedComplaint.t_type)]">
              {{ getTypeLabel(selectedComplaint.t_type) }}
            </span>
          </div>
          <div class="detail-row">
            <strong>{{ t('management.complaints.status.label') }}:</strong>
            <span :class="['status-badge', getStatusClass(selectedComplaint.t_status)]">
              {{ getStatusLabel(selectedComplaint.t_status) }}
            </span>
          </div>
          <div class="detail-row">
            <strong>{{ t('management.complaints.travelId') }}:</strong>
            <span>{{ selectedComplaint.k_id_travel }}</span>
          </div>
          <div class="detail-row full-width">
            <strong>{{ t('management.complaints.description') }}:</strong>
            <p class="description-full">{{ selectedComplaint.d_description }}</p>
          </div>
          <div class="detail-row">
            <strong>{{ t('management.complaints.createdAt') }}:</strong>
            <span>{{ formatDate(selectedComplaint.created_at) }}</span>
          </div>
          <div v-if="selectedComplaint.updated_at" class="detail-row">
            <strong>{{ t('management.complaints.updatedAt') }}:</strong>
            <span>{{ formatDate(selectedComplaint.updated_at) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useComplaintsStore } from '@/stores/complaintsStore';
import type Complaint from '@/models/Complaint';
import { ComplaintStatus, ComplaintType } from '@/models/Complaint';

const { t } = useI18n();
const complaintsStore = useComplaintsStore();

const filterStatus = ref<string>('all');
const selectedComplaint = ref<Complaint | null>(null);

const filteredComplaints = computed(() => {
  const complaints = Array.isArray(complaintsStore.complaints) ? complaintsStore.complaints : [];
  console.log('[CaCComponent] 📊 Total quejas en store:', complaints.length);
  console.log('[CaCComponent] 🔍 Filtro actual:', filterStatus.value);

  if (filterStatus.value === 'all') {
    return complaints;
  }

  const filtered = complaints.filter(c => c.t_status === filterStatus.value);
  console.log('[CaCComponent] ✅ Quejas filtradas:', filtered.length);
  return filtered;
});

function getStatusClass(status: string): string {
  const statusUpper = status.toUpperCase();
  switch (statusUpper) {
    case ComplaintStatus.OPEN:
      return 'status-open';
    case ComplaintStatus.IN_PROGRESS:
      return 'status-in-progress';
    case ComplaintStatus.RESOLVED:
      return 'status-resolved';
    case ComplaintStatus.CLOSED:
      return 'status-closed';
    default:
      return '';
  }
}

function getStatusLabel(status: string): string {
  const statusUpper = status.toUpperCase();
  switch (statusUpper) {
    case ComplaintStatus.OPEN:
      return t('management.complaints.status.open');
    case ComplaintStatus.IN_PROGRESS:
      return t('management.complaints.status.inProgress');
    case ComplaintStatus.RESOLVED:
      return t('management.complaints.status.resolved');
    case ComplaintStatus.CLOSED:
      return t('management.complaints.status.closed');
    default:
      return status;
  }
}

function getTypeClass(type: string): string {
  const typeUpper = type.toUpperCase();
  switch (typeUpper) {
    case ComplaintType.COMPLAINT:
      return 'type-complaint';
    case ComplaintType.CLAIM:
      return 'type-claim';
    case ComplaintType.SUGGESTION:
      return 'type-suggestion';
    default:
      return 'type-other';
  }
}

function getTypeLabel(type: string): string {
  const typeUpper = type.toUpperCase();
  switch (typeUpper) {
    case ComplaintType.COMPLAINT:
      return t('management.complaints.types.complaint');
    case ComplaintType.CLAIM:
      return t('management.complaints.types.claim');
    case ComplaintType.SUGGESTION:
      return t('management.complaints.types.suggestion');
    default:
      return t('management.complaints.types.other');
  }
}

function formatDate(date: Date | string | undefined): string {
  if (!date) return 'N/A';
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleDateString('es-CO', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

async function handleStatusChange(id: number, event: Event) {
  const target = event.target as HTMLSelectElement;
  const newStatus = target.value;

  try {
    await complaintsStore.updateComplaintStatus(id, newStatus);
    alert(t('management.complaints.updateSuccess'));
  } catch (error) {
    alert(t('management.complaints.updateError'));
    // Revertir el cambio en caso de error
    await complaintsStore.fetchComplaints();
  }
}

async function handleDelete(id: number) {
  if (!confirm(t('management.complaints.confirmDelete'))) {
    return;
  }

  try {
    await complaintsStore.deleteComplaint(id);
    alert(t('management.complaints.deleteSuccess'));
  } catch (error) {
    alert(t('management.complaints.deleteError'));
  }
}

function viewComplaint(complaint: Complaint) {
  selectedComplaint.value = complaint;
}

onMounted(async () => {
  console.log('[CaCComponent] 🚀 Componente montado, iniciando carga de quejas...');
  console.log('[CaCComponent] 🔗 Base URL:', complaintsStore.baseURL);

  try {
    // Siempre cargar las quejas al entrar al componente
    await complaintsStore.fetchComplaints();

    console.log('[CaCComponent] ✅ Quejas cargadas exitosamente:', complaintsStore.complaints.length);
    console.log('[CaCComponent] 📊 Estadísticas:', {
      loading: complaintsStore.loading,
      error: complaintsStore.error,
      total: complaintsStore.complaints.length,
      open: complaintsStore.openComplaints.length,
      inProgress: complaintsStore.inProgressComplaints.length,
      resolved: complaintsStore.resolvedComplaints.length,
      closed: complaintsStore.closedComplaints.length
    });

    if (complaintsStore.complaints.length > 0) {
      console.log('[CaCComponent] 📋 Ejemplo de queja:', complaintsStore.complaints[0]);
    }
  } catch (error) {
    console.error('[CaCComponent] ❌ Error al cargar quejas:', error);
  }
});
</script>

<style lang="scss" scoped>
.section {
  background: var(--color-surface);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;

  h2 {
    margin: 0;
    color: var(--color-text);
  }
}

.filters {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.filter-select {
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-background);
  color: var(--color-text);
  font-size: 0.95rem;
  cursor: pointer;

  &:focus {
    outline: none;
    border-color: var(--color-primary);
  }
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  border-radius: 8px;
  background: var(--color-background);
  border-left: 4px solid;

  &.open {
    border-color: #f44336;
    .material-symbols-outlined {
      color: #f44336;
    }
  }

  &.in-progress {
    border-color: #ff9800;
    .material-symbols-outlined {
      color: #ff9800;
    }
  }

  &.resolved {
    border-color: #4caf50;
    .material-symbols-outlined {
      color: #4caf50;
    }
  }

  &.closed {
    border-color: #757575;
    .material-symbols-outlined {
      color: #757575;
    }
  }

  .material-symbols-outlined {
    font-size: 2.5rem;
  }

  .stat-info {
    display: flex;
    flex-direction: column;
  }

  .stat-value {
    font-size: 2rem;
    font-weight: bold;
    color: var(--color-text);
  }

  .stat-label {
    font-size: 0.85rem;
    color: var(--color-text-secondary);
  }
}

.data-table {
  overflow-x: auto;
  margin-top: 1rem;

  table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.95rem;

    th, td {
      padding: 1rem;
      text-align: left;
      border-bottom: 1px solid var(--color-border);
    }

    th {
      background: var(--color-background);
      color: var(--color-text-secondary);
      font-weight: 600;
      white-space: nowrap;
    }

    tbody tr {
      transition: background-color 0.2s;

      &:hover {
        background: var(--color-hover);
      }
    }
  }
}

.description-cell {
  max-width: 300px;
}

.description-text {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.type-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 500;

  &.type-complaint {
    background: #fee2e2;
    color: #991b1b;
  }

  &.type-claim {
    background: #fef3c7;
    color: #92400e;
  }

  &.type-suggestion {
    background: #dbeafe;
    color: #1e40af;
  }

  &.type-other {
    background: #e5e7eb;
    color: #374151;
  }
}

.status-select {
  padding: 6px 12px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  font-weight: 500;

  &.status-open {
    background: #fee2e2;
    color: #991b1b;
    border-color: #f87171;
  }

  &.status-in-progress {
    background: #fef3c7;
    color: #92400e;
    border-color: #fbbf24;
  }

  &.status-resolved {
    background: #d1fae5;
    color: #065f46;
    border-color: #34d399;
  }

  &.status-closed {
    background: #e5e7eb;
    color: #374151;
    border-color: #9ca3af;
  }

  &:focus {
    outline: none;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }
}

.status-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 500;

  &.status-open {
    background: #fee2e2;
    color: #991b1b;
  }

  &.status-in-progress {
    background: #fef3c7;
    color: #92400e;
  }

  &.status-resolved {
    background: #d1fae5;
    color: #065f46;
  }

  &.status-closed {
    background: #e5e7eb;
    color: #374151;
  }
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.empty-message {
  text-align: center;
  padding: 3rem;
  color: var(--color-text-secondary);
  font-style: italic;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: var(--color-surface);
  border-radius: 12px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);

  h3 {
    margin: 0;
  }

  .btn-close {
    background: none;
    border: none;
    cursor: pointer;
    padding: 0.5rem;
    border-radius: 50%;
    transition: background 0.2s;

    &:hover {
      background: var(--color-hover);
    }

    .material-symbols-outlined {
      font-size: 1.5rem;
      color: var(--color-text-secondary);
    }
  }
}

.modal-body {
  padding: 1.5rem;
}

.detail-row {
  display: grid;
  grid-template-columns: 150px 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
  align-items: start;

  &.full-width {
    grid-template-columns: 1fr;
  }

  strong {
    color: var(--color-text-secondary);
  }

  .description-full {
    margin: 0.5rem 0 0;
    line-height: 1.6;
    color: var(--color-text);
  }
}

/* Buttons */
.btn-primary, .btn-secondary, .btn-danger, .btn-info {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  .material-symbols-outlined {
    font-size: 1.2rem;
  }

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  }
}

.btn-primary {
  background: var(--color-primary);
  color: white;
}

.btn-danger {
  background: #f44336;
  color: white;
}

.btn-info {
  background: #2196f3;
  color: white;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.85rem;

  .material-symbols-outlined {
    font-size: 1rem;
  }
}

/* Loading and Error States */
.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  gap: 1rem;
  color: var(--color-text-secondary);

  .material-symbols-outlined {
    font-size: 3rem;
    color: #f44336;
  }

  p {
    font-size: 1.1rem;
    margin: 0;
  }
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .data-table {
    font-size: 0.85rem;

    th, td {
      padding: 0.75rem 0.5rem;
    }
  }

  .detail-row {
    grid-template-columns: 1fr;
  }
}
</style>
