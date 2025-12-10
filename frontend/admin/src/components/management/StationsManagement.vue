<template>
  <div class="section">
    <div class="section-header">
      <h2>{{ t('management.stations.title') }}</h2>
      <button class="btn-primary" @click="showForm = true">
        <span class="material-symbols-outlined">add</span>
        {{ t('management.stations.create') }}
      </button>
    </div>

    <!-- Filter Section -->
    <div class="filter-section">
      <div class="filter-group">
        <label>{{ t('management.stations.filterByCity') }}</label>
        <select v-model="selectedCityFilter">
          <option :value="null">{{ t('management.stations.allCities') }}</option>
          <option v-for="city in cityStore.cities" :key="city.idCity" :value="city.idCity">
            {{ city.cityName }}
          </option>
        </select>
      </div>
      <div v-if="selectedCityFilter" class="filter-info">
        {{ t('management.stations.filtered', { count: filteredStations.length, total: stationStore.stations.length }) }}
      </div>
    </div>

    <div v-if="showForm" class="form-card">
      <h3>{{ t('management.stations.newStation') }}</h3>
      <form @submit.prevent="handleCreate">
        <div class="form-row">
          <div class="form-group">
            <label>{{ t('management.stations.idStation') }}</label>
            <input v-model.number="form.idStation" type="number" required />
          </div>
          <div class="form-group">
            <label>{{ t('management.stations.stationName') }}</label>
            <input v-model="form.stationName" type="text" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>{{ t('management.stations.latitude') }}</label>
            <input v-model.number="form.latitude" type="number" step="any" required />
          </div>
          <div class="form-group">
            <label>{{ t('management.stations.longitude') }}</label>
            <input v-model.number="form.length" type="number" step="any" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>{{ t('management.stations.city') }}</label>
            <select v-model.number="form.idCity" required>
              <option value="">{{ t('management.stations.selectCity') }}</option>
              <option v-for="city in cityStore.cities" :key="city.idCity" :value="city.idCity">
                {{ city.cityName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>{{ t('management.stations.type') }}</label>
            <select v-model="form.type" required>
              <option value="METRO">METRO</option>
              <option value="PLAZA">PLAZA</option>
              <option value="PARQUE">PARQUE</option>
              <option value="COMERCIAL">COMERCIAL</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label class="checkbox-label">
            <input v-model="form.cctvStatus" type="checkbox" />
            {{ t('management.stations.cctvStatus') }}
          </label>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn-primary" :disabled="stationStore.loading">
            {{ stationStore.loading ? t('common.saving') : t('common.save') }}
          </button>
          <button type="button" class="btn-secondary" @click="showForm = false">
            {{ t('common.cancel') }}
          </button>
        </div>
      </form>
    </div>

    <div class="data-table">
      <table v-if="filteredStations.length">
        <thead>
          <tr>
            <th>{{ t('common.id') }}</th>
            <th>{{ t('management.stations.stationName') }}</th>
            <th>{{ t('management.stations.city') }}</th>
            <th>{{ t('management.stations.type') }}</th>
            <th>{{ t('management.stations.location') }}</th>
            <th>{{ t('management.stations.cctv') }}</th>
            <th>{{ t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="station in filteredStations" :key="station.idStation">
            <td>{{ station.idStation }}</td>
            <td>{{ station.stationName }}</td>
            <td>{{ station.idCity ? getCityName(station.idCity) : '-' }}</td>
            <td>
              <span class="type-badge">
                {{ t('management.stations.types.' + ((station.type || 'METRO').toString().toLowerCase())) }}
              </span>
            </td>
            <td>{{ station.latitude.toFixed(4) }}, {{ (station.length ?? station.longitude ?? 0).toFixed(4) }}</td>
            <td>
              <span :class="['status-indicator', station.cctvStatus ? 'active' : 'inactive']" :aria-label="station.cctvStatus ? t('common.active') : t('common.inactive')">
                {{ station.cctvStatus ? '●' : '○' }}
              </span>
            </td>
            <td>
              <button class="btn-info btn-sm" @click="$emit('view-slots', station.idStation)">
                <span class="material-symbols-outlined">grid_view</span>
              </button>
              <button class="btn-danger btn-sm" @click="handleDelete(station.idStation)">
                <span class="material-symbols-outlined">delete</span>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty-message">{{ t('management.stations.empty') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
/* eslint-disable no-undef */
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useCityStore } from '@/stores/cityStore';
import { useStationStore } from '@/stores/stationStore';

const { t } = useI18n();
const cityStore = useCityStore();
const stationStore = useStationStore();

defineEmits<{
  (e: 'view-slots', stationId: number): void;
}>();

const showForm = ref(false);
const selectedCityFilter = ref<number | null>(null);

const getNextId = () => {
  const ids = stationStore.stations.map(s => s.idStation);
  return ids.length > 0 ? Math.max(...ids) + 1 : 1;
};

const form = ref({
  idStation: getNextId(),
  stationName: '',
  latitude: 0,
  length: 0,
  idCity: 0,
  type: 'METRO',
  cctvStatus: true,
});

const filteredStations = computed(() => {
  if (selectedCityFilter.value === null) {
    return stationStore.stations;
  }
  return stationStore.stations.filter(station => station.idCity === selectedCityFilter.value);
});

function getCityName(cityId: number): string {
  const city = cityStore.cities.find(c => c.idCity === cityId);
  return city ? city.cityName : t('management.stations.unknownCity', { id: cityId });
}

async function handleCreate() {
  try {
    await stationStore.createStation(form.value);
    showForm.value = false;
    form.value = {
      idStation: getNextId(),
      stationName: '',
      latitude: 0,
      length: 0,
      idCity: 0,
      type: 'METRO',
      cctvStatus: true,
    };
    alert(t('management.stations.createSuccess'));
  } catch (error) {
    const errorMsg = error instanceof Error ? error.message : t('management.stations.createError');
    alert(`${t('management.stations.createError')}: ${errorMsg}`);
    console.error('Create station error:', error);
  }
}

async function handleDelete(id: number) {
  if (confirm(t('management.stations.confirmDelete'))) {
    try {
      await stationStore.deleteStation(id);
      alert(t('management.stations.deleteSuccess'));
    } catch (error) {
      alert(t('management.stations.deleteError'));
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/management-shared-styles.scss';

.type-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
  background: var(--color-primary);
  color: white;
}

.status-indicator {
  font-size: 1.5rem;

  &.active {
    color: #10b981;
  }

  &.inactive {
    color: #9ca3af;
  }
}
</style>
