<template>
  <div class="section">
    <div class="section-header">
      <h2>{{ t('management.maintenance.title') }}</h2>
    </div>
    <div class="form-card">
      <h3>{{ isEditing ? t('management.maintenance.edit') : t('management.maintenance.newOrder') }}</h3>
      <form @submit.prevent="handleSubmit">
        <div class="form-row">
          <div class="form-group">
            <label>{{ t('management.maintenance.entityType') }}</label>
            <select v-model="form.entityType" required>
              <option value="BICYCLE">{{ t('management.maintenance.entities.bicycle') }}</option>
              <option value="STATION">{{ t('management.maintenance.entities.station') }}</option>
              <option value="LOCK">{{ t('management.maintenance.entities.lock') }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>{{ t('management.maintenance.maintenanceType') }}</label>
            <select v-model="form.maintenanceType" required>
              <option value="PREVENTIVE">{{ t('management.maintenance.types.preventive') }}</option>
              <option value="CORRECTIVE">{{ t('management.maintenance.types.corrective') }}</option>
              <option value="INSPECTION">{{ t('management.maintenance.types.inspection') }}</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>{{ t('management.maintenance.triggeredBy') }}</label>
            <select v-model="form.triggeredBy" required>
              <option value="ADMIN">{{ t('management.maintenance.triggers.admin') }}</option>
              <option value="IOT_ALERT">{{ t('management.maintenance.triggers.iotAlert') }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>{{ t('management.maintenance.description') }}</label>
            <input v-model="form.description" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>{{ t('management.maintenance.status') }}</label>
            <select v-model="form.status" required>
              <option value="PENDING">{{ t('management.maintenance.statuses.pending') }}</option>
              <option value="SOLVING">{{ t('management.maintenance.statuses.solving') }}</option>
              <option value="RESOLVED">{{ t('management.maintenance.statuses.resolved') }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>{{ t('management.maintenance.date') }}</label>
            <input type="date" v-model="form.date" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>{{ t('management.maintenance.cost') }}</label>
            <input type="number" v-model.number="form.cost" min="0" required />
          </div>
          <div class="form-group">
            <label>{{ t('management.maintenance.orderId') }}</label>
            <input v-model="form.id" :disabled="isEditing" />
          </div>
        </div>
        <div class="form-row">
          <div v-if="form.entityType === 'BICYCLE'" class="form-group">
            <label>{{ t('management.maintenance.bikeId') }} <span style="color:red">*</span></label>
            <input v-model="form.bikeId" required />
          </div>
          <div v-if="form.entityType === 'STATION'" class="form-group">
            <label>{{ t('management.maintenance.stationId') }} <span style="color:red">*</span></label>
            <input v-model="form.stationId" required />
          </div>
          <div v-if="form.entityType === 'LOCK'" class="form-group">
            <label>{{ t('management.maintenance.lockId') }} <span style="color:red">*</span></label>
            <input v-model="form.lockId" required />
          </div>
        </div>
        <div class="form-actions">
          <button class="btn-primary" type="submit" :disabled="!isFormValid || loading">
            {{ isEditing ? t('management.maintenance.saveChanges') : t('management.maintenance.create') }}
          </button>
          <button v-if="isEditing" class="btn-secondary" type="button" @click="resetForm">{{ t('management.maintenance.cancel') }}</button>
        </div>
      </form>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>{{ t('management.maintenance.loading') }}</p>
    </div>
    <div v-else-if="error" class="error-state">
      <span class="material-symbols-outlined">error</span>
      <p>{{ error }}</p>
    </div>
    <div v-else class="data-table">
      <table v-if="maintenances.length">
        <thead>
          <tr>
            <th>{{ t('management.maintenance.entityType') }}</th>
            <th>{{ t('management.maintenance.maintenanceType') }}</th>
            <th>{{ t('management.maintenance.triggeredBy') }}</th>
            <th>{{ t('management.maintenance.description') }}</th>
            <th>{{ t('management.maintenance.status') }}</th>
            <th>{{ t('management.maintenance.date') }}</th>
            <th>{{ t('management.maintenance.cost') }}</th>
            <th>{{ t('management.maintenance.bikeId') }}</th>
            <th>{{ t('management.maintenance.stationId') }}</th>
            <th>{{ t('management.maintenance.lockId') }}</th>
            <th>{{ t('management.maintenance.orderId') }}</th>
            <th>{{ t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="m in maintenances" :key="m.id">
            <td>{{ m.entityType }}</td>
            <td>{{ m.maintenanceType }}</td>
            <td>{{ m.triggeredBy }}</td>
            <td>{{ m.description }}</td>
            <td>{{ m.status }}</td>
            <td>{{ formatDate(m.date) }}</td>
            <td>{{ m.cost }}</td>
            <td>{{ m.bikeId }}</td>
            <td>{{ m.stationId }}</td>
            <td>{{ m.lockId }}</td>
            <td>{{ m.id }}</td>
            <td>
              <button class="btn-info btn-sm" type="button" @click="startEdit(m)">
                <span class="material-symbols-outlined">edit</span>
                {{ t('management.maintenance.editButton') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty-message">{{ t('management.maintenance.empty') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import type Maintenance from '@/models/Maintenance';
import { useMaintenanceStore } from '@/stores/maintenanceStore';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const maintenanceStore = useMaintenanceStore();
const loading = computed(() => maintenanceStore.loading);
const error = ref<string | null>(null);
const maintenances = computed(() => maintenanceStore.maintList);

const initialFormState = {
  entityType: 'BICYCLE',
  maintenanceType: 'PREVENTIVE',
  triggeredBy: 'ADMIN',
  description: '',
  status: 'PENDING',
  date: '',
  cost: 0,
  bikeId: '',
  stationId: '',
  lockId: '',
  id: ''
};

const form = ref({ ...initialFormState });
const isEditing = ref(false);
const editingId = ref<string | null>(null);

const isFormValid = computed(() => {
  if (form.value.entityType === 'BICYCLE') {
    return !!form.value.bikeId;
  }
  if (form.value.entityType === 'STATION') {
    return !!form.value.stationId;
  }
  if (form.value.entityType === 'LOCK') {
    return !!form.value.lockId;
  }
  return false;
});

function formatDate(date: string | Date) {
  if (!date) return '';
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleDateString('es-CO', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

function normalizePayload() {
  const stationId = form.value.stationId ? Number(form.value.stationId) : null;
  return {
    entityType: form.value.entityType,
    maintenanceType: form.value.maintenanceType,
    triggeredBy: form.value.triggeredBy,
    description: form.value.description,
    status: form.value.status,
    date: form.value.date,
    cost: form.value.cost,
    bikeId: form.value.entityType === 'BICYCLE' ? form.value.bikeId : null,
    stationId: form.value.entityType === 'STATION' ? stationId : null,
    lockId: form.value.entityType === 'LOCK' ? form.value.lockId : null,
  };
}

function resetForm() {
  Object.assign(form.value, initialFormState);
  isEditing.value = false;
  editingId.value = null;
}

function startEdit(record: Maintenance) {
  const recordId = (record as Maintenance).id;
  if (recordId === undefined || recordId === null) {
    error.value = 'No se puede editar esta orden porque no tiene identificador.';
    return;
  }

  error.value = null;
  isEditing.value = true;
  editingId.value = String(recordId);
  const recordDate = record.date ? new Date(record.date as unknown as string) : null;
  Object.assign(form.value, {
    entityType: record.entityType,
    maintenanceType: record.maintenanceType,
    triggeredBy: record.triggeredBy,
    description: record.description,
    status: record.status,
    date: recordDate ? recordDate.toISOString().slice(0, 10) : '',
    cost: record.cost ?? 0,
    bikeId: record.bikeId || '',
    stationId: record.stationId ?? '',
    lockId: record.lockId || '',
    id: String(recordId),
  });
}

async function handleSubmit() {
  error.value = null;
  const payload = normalizePayload();

  try {
    if (isEditing.value && editingId.value) {
      await maintenanceStore.updateMaintenance(editingId.value, payload);
    } else {
      await maintenanceStore.createMaintenance({ ...payload, id: form.value.id || undefined });
    }
    await maintenanceStore.fetchMaintenances();
    resetForm();
  } catch (err: unknown) {
    const message = err instanceof Error
      ? err.message
      : isEditing.value
        ? 'Error al actualizar la orden'
        : 'Error al crear la orden';
    error.value = message;
  }
}

onMounted(() => {
  maintenanceStore.fetchMaintenances();
});
</script>

<style lang="scss" scoped>
@import '@/styles/management-shared-styles.scss';
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
}
  .form-card {
    margin-bottom: 2rem;
    background: var(--color-background);
    padding: 1.5rem;
    border-radius: 8px;
    box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  }
  .form-card h3 {
    margin-top: 0;
    margin-bottom: 1.5rem;
    font-size: 1.2rem;
    font-weight: 600;
  }
  .form-row {
    display: flex;
    gap: 1.2rem;
    margin-bottom: 1rem;
    flex-wrap: wrap;
    justify-content: center;
  }
  .form-group {
    display: flex;
    flex-direction: column;
    flex: 1 1 320px;
    max-width: 400px;
    min-width: 220px;
    background: var(--color-surface-alt, #f7f7fa);
    border: 1.5px solid var(--color-border);
    border-radius: 8px;
    box-shadow: 0 1px 4px rgba(0,0,0,0.04);
    padding: 1rem 1.2rem 0.7rem 1.2rem;
    margin-bottom: 0.2rem;
    transition: box-shadow 0.2s, border-color 0.2s;
  }
  .form-group:not(:last-child) {
    margin-right: 0.5rem;
  }
  .form-group label {
    font-weight: 600;
    margin-bottom: 0.5rem;
    color: var(--color-text-secondary);
    letter-spacing: 0.01em;
  }
  .form-group input, .form-group select {
    padding: 0.6rem 1.1rem;
    border: 1.5px solid var(--color-border);
    border-radius: 6px;
    background: var(--color-background);
    color: var(--color-text);
    font-size: 1rem;
    margin-bottom: 0.1rem;
    outline: none;
    transition: border-color 0.2s, box-shadow 0.2s;
  }
  .form-group input:focus, .form-group select:focus {
    border-color: var(--color-primary-light);
    box-shadow: 0 0 0 2px rgba(46, 125, 50, 0.12);
  }
  .form-group input:hover, .form-group select:hover {
    border-color: var(--color-primary-light);
  }
  .form-actions {
    display: flex;
    gap: 1rem;
    margin-top: 1.5rem;
  }
  .btn-primary {
    background: var(--color-primary-light);
    color: white;
    border: none;
    border-radius: 6px;
    padding: 0.75rem 1.5rem;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.2s;
  }
  .btn-primary:hover, .btn-primary:focus {
    background: var(--color-primary-dark, #1e5a1f);
  }
.data-table {
  overflow-x: auto;
  margin-top: 1rem;
}
.data-table table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.95rem;
}
.data-table th, .data-table td {
  padding: 1rem;
  text-align: center;
  border-bottom: 1px solid var(--color-border);
}
.data-table th:first-child, .data-table td:first-child {
  text-align: left;
}
.data-table th {
  background: var(--color-background);
  color: var(--color-text-secondary);
  font-weight: 600;
  white-space: nowrap;
}
.empty-message {
  text-align: center;
  padding: 3rem;
  color: var(--color-text-secondary);
  font-style: italic;
}
.loading-state, .error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  gap: 1rem;
  color: var(--color-text-secondary);
}
.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--color-border);
  border-top-color: var(--color-primary-light);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
