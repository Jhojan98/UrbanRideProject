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
              <option value="RESIDENTIAL">RESIDENTIAL</option>
              <option value="FINANCIAL CENTER">FINANCIAL CENTER</option>
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
              <div style="display: flex; align-items: center; justify-content: center;">
                <span :class="['station-type-icon', (station.type || 'METRO').toLowerCase()]">
                  <i v-if="(station.type || 'METRO').toLowerCase() === 'metro'" class="fa fa-subway"></i>
                  <i v-else-if="(station.type || 'METRO').toLowerCase() === 'residential'" class="fa fa-building"></i>
                  <i v-else-if="(station.type || 'METRO').toLowerCase() === 'financial'" class="fa fa-briefcase"></i>
                </span>
                <span :class="['type-badge', (station.type || 'METRO').toLowerCase()]">
                  {{ t('management.stations.types.' + ((station.type || 'METRO').toString().toLowerCase())) }}
                </span>
              </div>
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

.data-table {
  table {
    width: 100%;
    border-collapse: collapse;
    
    thead {
      background-color: #f5f5f5;
    }
    
    th {
      padding: 12px;
      text-align: left;
      font-weight: 600;
      color: #333;
      border-bottom: 2px solid #ddd;
    }
    
    td {
      padding: 10px 12px;
      border-bottom: 1px solid #e0e0e0;
      text-align: left;
    }
    
    tbody tr {
      &:hover {
        background-color: #f9f9f9;
      }
    }
  }
}

.type-badge {
  display: inline-block;
  padding: 0.35rem 0.85rem;
  border-radius: 14px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  color: white;
  
  &:has(+ *:contains('metro')) {
    background-color: #1e88e5;
  }
  
  background-color: #1e88e5;
  
  &.metro {
    background-color: #1e88e5;
  }
  
  &.financial {
    background-color: #f59e0b;
  }
  
  &.residential {
    background-color: #10b981;
  }
}

.status-indicator {
  font-size: 1.5rem;
  display: inline-block;

  &.active {
    color: #10b981;
  }

  &.inactive {
    color: #9ca3af;
  }
}

.btn-sm {
  padding: 0.4rem 0.6rem;
  font-size: 0.8rem;
  margin: 0 0.2rem;
  
  span {
    font-size: 1.1rem;
  }
}

button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  
  span {
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  
  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  }
}

.btn-primary {
  background-color: #007bff;
  color: white;
  
  &:hover:not(:disabled) {
    background-color: #0056b3;
  }
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
  
  &:hover:not(:disabled) {
    background-color: #545b62;
  }
}

.btn-info {
  background-color: #17a2b8;
  color: white;
  padding: 0.4rem 0.6rem;
  
  &:hover:not(:disabled) {
    background-color: #138496;
  }
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  padding-top: 1.5rem;
  border-top: 1px solid #e0e0e0;
  
  button {
    flex: 0 1 auto;
    min-width: 120px;
  }
}
</style>
