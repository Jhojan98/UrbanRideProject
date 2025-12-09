<template>
  <div class="section">
    <div class="section-header">
      <h2>{{ t('management.slots.title') }}</h2>
      <div class="filter-group">
        <label>{{ t('management.slots.filterByStation') }}</label>
        <select v-model="selectedStationFilter">
          <option :value="null">{{ t('management.slots.allStations') }}</option>
          <option v-for="station in stationStore.stations" :key="station.idStation" :value="station.idStation">
            {{ station.stationName }}
          </option>
        </select>
      </div>
    </div>

    <div class="slots-grid">
        <div
          v-for="slot in filteredSlots"
          :key="slot.idSlot"
          :class="['slot-card', slot.padlockStatus.toString().toLowerCase()]"
        >
        <div class="slot-header">
          <h4>{{ slot.idSlot }}</h4>
          <span :class="['slot-status', slot.padlockStatus.toString().toLowerCase()]">
            {{ t('management.slots.status.' + slot.padlockStatus.toString().toLowerCase()) }}
          </span>
        </div>
        <div class="slot-body">
          <p><strong>{{ t('management.slots.station') }}:</strong> {{ getStationName(slot.stationId) }}</p>
          <p>
            <strong>{{ t('management.slots.bicycle') }}:</strong>
            <code v-if="slot.bicycleId">{{ slot.bicycleId }}</code>
            <span v-else class="text-muted">{{ t('management.slots.noBicycle') }}</span>
          </p>
        </div>
        <div v-if="!slot.bicycleId && slot.padlockStatus === 'UNLOCKED'" class="slot-actions">
          <button class="btn-primary btn-sm" @click="openAssignDialog(slot)">
            <span class="material-symbols-outlined">link</span>
            {{ t('management.slots.assignBicycle') }}
          </button>
        </div>
      </div>
    </div>

    <p v-if="filteredSlots.length === 0" class="empty-message">
      {{ t('management.slots.empty') }}
    </p>

    <!-- Modal para asignar bicicleta -->
    <div v-if="showAssignDialog" class="modal-overlay" @click="showAssignDialog = false">
      <div class="modal-content" @click.stop>
        <h3>{{ t('management.slots.assignBicycleTitle') }}</h3>
        <p>{{ t('management.slots.assigningToSlot') }}: <strong>{{ selectedSlot?.idSlot }}</strong></p>

        <div class="form-group">
          <label>{{ t('management.slots.selectBicycle') }}</label>
          <select v-model="selectedBicycleId">
            <option value="">{{ t('management.slots.chooseBicycle') }}</option>
            <option
              v-for="bicycle in availableBicycles"
              :key="bicycle.idBicycle"
              :value="bicycle.idBicycle"
            >
              {{ bicycle.idBicycle }} - {{ bicycle.model }} ({{ bicycle.padlockStatus }})
            </option>
          </select>
        </div>

        <div class="form-actions">
          <button
            class="btn-primary"
            @click="handleAssign"
            :disabled="!selectedBicycleId || stationStore.loading"
          >
            {{ stationStore.loading ? t('common.assigning') : t('common.assign') }}
          </button>
          <button class="btn-secondary" @click="showAssignDialog = false">
            {{ t('common.cancel') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/* eslint-disable no-undef */
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useStationStore } from '@/stores/stationStore';
import { useBikeStore } from '@/stores/bikeStore';
import type { AdminSlot } from '@/models/AdminSlot';

const { t } = useI18n();
const stationStore = useStationStore();
const bicycleStore = useBikeStore();

const props = defineProps<{
  initialStationId?: number | null;
}>();

const selectedStationFilter = ref<number | null>(props.initialStationId || null);
const showAssignDialog = ref(false);
const selectedSlot = ref<AdminSlot | null>(null);
const selectedBicycleId = ref('');

const filteredSlots = computed(() => {
  if (selectedStationFilter.value === null) {
    return stationStore.slots;
  }
  return stationStore.slots.filter(slot => slot.stationId === selectedStationFilter.value);
});

const availableBicycles = computed(() => {
  return bicycleStore.bicycles.filter(b => b.padlockStatus === 'UNLOCKED');
});

function getStationName(stationId: number): string {
  const station = stationStore.stations.find(s => s.idStation === stationId);
  return station ? (station.nameStation || station.stationName || t('management.stations.unknownStation', { id: stationId })) : t('management.stations.unknownStation', { id: stationId });
}

function openAssignDialog(slot: AdminSlot) {
  selectedSlot.value = slot;
  selectedBicycleId.value = '';
  showAssignDialog.value = true;
}

async function handleAssign() {
  if (!selectedSlot.value || !selectedBicycleId.value) return;

  try {
    await stationStore.assignBicycleToSlot(selectedSlot.value.idSlot, selectedBicycleId.value);
    showAssignDialog.value = false;
    selectedSlot.value = null;
    selectedBicycleId.value = '';
    await bicycleStore.fetchBicycles();
    alert(t('management.slots.assignSuccess'));
  } catch (error) {
    alert(t('management.slots.assignError'));
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/management-shared-styles.scss';

.filter-group {
  display: flex;
  align-items: center;
  gap: 1rem;

  label {
    font-weight: 500;
    color: var(--color-text-primary-light);
  }

  select {
    padding: 0.5rem 1rem;
    border: 1px solid var(--color-border-light);
    border-radius: 4px;
    font-size: 0.9rem;
    background: var(--color-background-light);
    color: var(--color-text-primary-light);
  }
}

[data-theme="dark"] .filter-group {
  label {
    color: var(--color-text-primary-dark);
  }

  select {
    background: var(--color-surface-dark);
    color: var(--color-text-primary-dark);
    border-color: var(--color-border-dark);
  }
}

.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.slot-card {
  background: var(--color-background-light);
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
  color: var(--color-text-primary-light);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  &.locked {
    border-left: 4px solid #10b981;
  }

  &.unlocked {
    border-left: 4px solid #f59e0b;
  }

  &.reserved {
    border-left: 4px solid #3b82f6;
  }
}

[data-theme="dark"] .slot-card {
  background: var(--color-surface-dark);
  color: var(--color-text-primary-dark);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
  }
}

.slot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--color-border-light);

  h4 {
    margin: 0;
    font-family: monospace;
    font-size: 1.1rem;
    color: var(--color-text-primary-light);
  }
}

[data-theme="dark"] .slot-header {
  border-bottom-color: var(--color-border-dark);

  h4 {
    color: var(--color-text-primary-dark);
  }
}

.slot-status {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;

  &.locked {
    background: #d1fae5;
    color: #065f46;
  }

  &.unlocked {
    background: #fef3c7;
    color: #92400e;
  }

  &.reserved {
    background: #dbeafe;
    color: #1e40af;
  }
}

[data-theme="dark"] .slot-status {
  &.locked {
    background: #064e3b;
    color: #a7f3d0;
  }

  &.unlocked {
    background: #78350f;
    color: #fde68a;
  }

  &.reserved {
    background: #1e3a8a;
    color: #bfdbfe;
  }
}

.slot-body {
  margin-bottom: 1rem;

  p {
    margin: 0.5rem 0;
    font-size: 0.9rem;
    color: var(--color-text-primary-light);
  }

  code {
    background: var(--color-gray-very-light);
    padding: 0.2rem 0.5rem;
    border-radius: 4px;
    font-family: monospace;
    font-size: 0.9rem;
    color: var(--color-text-primary-light);
  }

  .text-muted {
    color: var(--color-text-secondary-light);
    font-style: italic;
  }
}

[data-theme="dark"] .slot-body {
  p {
    color: var(--color-text-primary-dark);
  }

  code {
    background: var(--color-background-dark);
    color: var(--color-text-primary-dark);
  }

  .text-muted {
    color: var(--color-text-secondary-dark);
  }
}

.slot-actions {
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border-light);
}

[data-theme="dark"] .slot-actions {
  border-top-color: var(--color-border-dark);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s;
}

[data-theme="dark"] .modal-overlay {
  background: rgba(0, 0, 0, 0.75);
}

.modal-content {
  background: var(--color-background-light);
  padding: 2rem;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s;
  color: var(--color-text-primary-light);

  h3 {
    margin-top: 0;
    color: var(--color-text-primary-light);
  }

  p {
    color: var(--color-text-primary-light);
  }
}

[data-theme="dark"] .modal-content {
  background: var(--color-surface-dark);
  color: var(--color-text-primary-dark);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);

  h3, p {
    color: var(--color-text-primary-dark);
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
