<template>
  <div class="section">
    <div class="section-header">
      <h2>{{ t('management.redistribution.title') }}</h2>
    </div>

    <div class="form-card">
      <h3>{{ t('management.redistribution.selectItems') }}</h3>
      <form @submit.prevent="handleRedistribute">
        <!-- Paso 1: Seleccionar bicicleta -->
        <div class="form-group">
          <label>{{ t('management.redistribution.selectBike') }}</label>
          <div v-if="bikesLoading" class="loading-text">{{ t('common.loading') }}</div>
          <select v-else v-model="selectedBikeId" required>
            <option value="">{{ t('management.redistribution.chooseBike') }}</option>
            <option v-for="b in bikeStore.bikes" :key="bikeId(b)" :value="bikeId(b)">
              {{ bikeId(b) }} — {{ bikeLabel(b) }}
            </option>
          </select>
        </div>

        <!-- Paso 2: Seleccionar estación -->
        <div class="form-group">
          <label>{{ t('management.redistribution.selectStation') }}</label>
          <select v-model.number="selectedStationId" @change="onStationChange" required>
            <option :value="null">{{ t('management.redistribution.chooseStation') }}</option>
            <option v-for="s in stationStore.stations" :key="s.idStation" :value="s.idStation">
              {{ s.stationName || s.nameStation || ('ID: ' + s.idStation) }}
              ({{ s.availableSlots }}/{{ s.totalSlots }} slots)
            </option>
          </select>
        </div>

        <!-- Paso 3: Seleccionar slot destino -->
        <div v-if="slots.length" class="form-group">
          <label>{{ t('management.redistribution.selectSlot') }}</label>
          <select v-model="selectedSlotId" required>
            <option value="">{{ t('management.redistribution.chooseSlot') }}</option>
            <option v-for="slot in slots" :key="slot.idSlot" :value="slot.idSlot">
              {{ slot.idSlot }} — {{ slot.padlockStatus }} — {{ t('management.redistribution.bike') }}: {{ slot.bicycleId || '-' }}
            </option>
          </select>
        </div>
        <div v-else-if="selectedStationId" class="info-text">
          {{ t('management.redistribution.noSlotsAvailable') }}
        </div>

        <!-- Información de la bicicleta seleccionada -->
        <div v-if="selectedBike" class="info-card">
          <h4>{{ t('management.redistribution.selectedBikeInfo') }}</h4>
          <div class="info-grid">
            <div>
              <strong>{{ t('common.id') }}:</strong> {{ bikeId(selectedBike) }}
            </div>
            <div>
              <strong>{{ t('management.redistribution.type') }}:</strong> {{ selectedBike.model }}
            </div>
            <div>
              <strong>{{ t('management.redistribution.series') }}:</strong> {{ selectedBike.series }}
            </div>
            <div v-if="selectedBike.battery">
              <strong>{{ t('management.redistribution.battery') }}:</strong> {{ selectedBike.battery }}
            </div>
          </div>
        </div>

        <!-- Botones de acción -->
        <div class="form-actions">
          <button
            type="submit"
            class="btn-primary"
            :disabled="!canAssign || assigning"
          >
            {{ assigning ? t('common.processing') : t('management.redistribution.redistribute') }}
          </button>
        </div>

        <!-- Mensajes -->
        <div v-if="error" class="message error">{{ error }}</div>
        <div v-if="success" class="message success">{{ success }}</div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useBicycleStore } from '@/stores/bikeStore';
import { useStationStore } from '@/stores/stationStore';
import { BikeHelpers } from '@/models/Bike';

const { t } = useI18n();
const bikeStore = useBicycleStore();
const stationStore = useStationStore();

const selectedBikeId = ref<string>('');
const selectedStationId = ref<number | null>(null);
const selectedSlotId = ref<string>('');

const slots = ref([]);
const assigning = ref(false);
const bikesLoading = ref(false);
const error = ref<string | null>(null);
const success = ref<string | null>(null);

function bikeId(b: any) {
  return BikeHelpers.getId(b);
}

function bikeLabel(b: any) {
  return `serie:${b.series} model:${b.model}`;
}

const selectedBike = computed(() => {
  if (!selectedBikeId.value) return null;
  return bikeStore.bikes.find(b => bikeId(b) === selectedBikeId.value) || null;
});

const canAssign = computed(() => !!selectedBikeId.value && !!selectedSlotId.value);

onMounted(async () => {
  bikesLoading.value = true;
  try {
    await bikeStore.fetchBikes();
    await stationStore.fetchStations();
    await stationStore.fetchSlots();
  } catch (err) {
    console.error(err);
    error.value = t('management.redistribution.loadError');
  } finally {
    bikesLoading.value = false;
  }
});

async function onStationChange() {
  selectedSlotId.value = '';
  try {
    await stationStore.fetchSlots();
    slots.value = stationStore.getSlotsByStation(Number(selectedStationId.value));
  } catch (err) {
    console.error(err);
    error.value = t('management.redistribution.slotLoadError');
  }
}

async function handleRedistribute() {
  error.value = null;
  success.value = null;

  if (!canAssign.value) {
    error.value = t('management.redistribution.selectRequired');
    return;
  }

  assigning.value = true;
  try {
    const res = await stationStore.assignBicycleToSlot(selectedSlotId.value, selectedBikeId.value);
    await stationStore.fetchSlots();
    success.value = t('management.redistribution.success', { slotId: selectedSlotId.value });

    // Resetear formulario
    setTimeout(() => {
      selectedBikeId.value = '';
      selectedStationId.value = null;
      selectedSlotId.value = '';
      slots.value = [];
      success.value = null;
    }, 2000);
  } catch (err: any) {
    error.value = err?.message || t('management.redistribution.error');
  } finally {
    assigning.value = false;
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/management-shared-styles.scss';

.loading-text {
  color: var(--color-text-secondary-light);
  font-size: 0.9rem;
  font-style: italic;
  padding: 0.5rem 0;
}

.info-card {
  background: var(--color-background-light);
  border-left: 4px solid var(--color-primary-light);
  padding: 1rem;
  border-radius: 6px;
  margin: 1.5rem 0;

  h4 {
    margin: 0 0 0.75rem;
    color: var(--color-text-primary-light);
    font-size: 1rem;
  }

  .info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 1rem;

    div {
      font-size: 0.9rem;
      color: var(--color-text-secondary-light);

      strong {
        color: var(--color-text-primary-light);
        display: block;
        margin-bottom: 0.25rem;
      }
    }
  }
}

[data-theme="dark"] .info-card {
  background: var(--color-surface-dark);
  border-left-color: var(--color-primary-light);

  h4,
  .info-grid strong {
    color: var(--color-text-primary-dark);
  }

  .info-grid div {
    color: var(--color-text-secondary-dark);
  }
}

.info-text {
  padding: 1rem;
  background: var(--color-gray-very-light);
  border-left: 4px solid var(--color-primary-light);
  border-radius: 6px;
  color: var(--color-text-secondary-light);
  font-size: 0.9rem;
  margin: 1rem 0;
}

[data-theme="dark"] .info-text {
  background: var(--color-surface-dark);
  color: var(--color-text-secondary-dark);
}

.message {
  padding: 0.75rem 1rem;
  border-radius: 6px;
  margin-top: 1rem;
  font-weight: 500;
  font-size: 0.9rem;

  &.error {
    background: #fee2e2;
    color: #991b1b;
    border: 1px solid #fecdd3;
  }

  &.success {
    background: #dcfce7;
    color: #166534;
    border: 1px solid #86efac;
  }
}

[data-theme="dark"] .message {
  &.error {
    background: rgba(220, 38, 38, 0.1);
    color: #fca5a5;
    border-color: rgba(220, 38, 38, 0.3);
  }

  &.success {
    background: rgba(34, 197, 94, 0.1);
    color: #86efac;
    border-color: rgba(34, 197, 94, 0.3);
  }
}
</style>