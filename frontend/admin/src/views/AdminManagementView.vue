<template>
  <div class="admin-management-view">
    <div class="management-container">
      <div class="management-header">
        <h2>Gestión Administrativa</h2>
      </div>

      <div class="management-body">
        <div class="tabs">
          <button
            :class="['tab', { active: activeTab === 'cities' }]"
            @click="activeTab = 'cities'"
          >
            <span class="material-symbols-outlined">location_city</span>
            Ciudades ({{ cityStore.cities.length }})
          </button>
          <button
            :class="['tab', { active: activeTab === 'stations' }]"
            @click="activeTab = 'stations'"
          >
            <span class="material-symbols-outlined">store</span>
            Estaciones ({{ stationStore.stations.length }})
          </button>
          <button
            :class="['tab', { active: activeTab === 'bicycles' }]"
            @click="activeTab = 'bicycles'"
          >
            <span class="material-symbols-outlined">pedal_bike</span>
            Bicicletas ({{ bicycleStore.bicycles.length }})
          </button>
          <button
            :class="['tab', { active: activeTab === 'slots' }]"
            @click="activeTab = 'slots'"
          >
            <span class="material-symbols-outlined">grid_view</span>
            Slots ({{ stationStore.slots.length }})
          </button>
          <button
            :class="['tab', { active: activeTab === 'redistribution' }]"
            @click="activeTab = 'redistribution'"
          >
            <span class="material-symbols-outlined">redo</span>
            Redistribución
          </button>
          <button
            :class="['tab', { active: activeTab === 'maintenances' }]"
            @click="activeTab = 'maintenances'"
          >
            <span class="material-symbols-outlined">build</span>
            Mantenimientos
          </button>
        </div>

        <div v-if="loading" class="loading">Cargando información...</div>

        <!-- Ciudades -->
        <div v-if="activeTab === 'cities'">
          <CitiesManagement />
        </div>

        <!-- Estaciones -->
        <div v-if="activeTab === 'stations'">
          <StationsManagement @view-slots="handleViewSlots" />
        </div>

        <!-- Bicicletas -->
        <div v-if="activeTab === 'bicycles'">
          <BicyclesManagement />
        </div>

        <!-- Slots -->
        <div v-if="activeTab === 'slots'">
          <SlotsManagement :initial-station-id="selectedStationForSlots" />
        </div>

        <!-- Redistribución -->
        <div v-if="activeTab === 'redistribution'">
          <BikeRedistributionManagement />
        </div>

        <!-- Mantenimientos -->
        <div v-if="activeTab === 'maintenances'">
          <MaintenancesManagement />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useCityStore } from '@/stores/cityStore';
import { useStationStore } from '@/stores/stationStore';
import { useBikeStore } from '@/stores/bikeStore';
import CitiesManagement from '@/components/management/CitiesManagement.vue';
import StationsManagement from '@/components/management/StationsManagement.vue';
import BicyclesManagement from '@/components/management/BicyclesManagement.vue';
import SlotsManagement from '@/components/management/SlotsManagement.vue';
import BikeRedistributionManagement from '@/components/management/BikeRedistributionManagement.vue';
import MaintenancesManagement from '@/components/management/MaintenancesManagement.vue';

const cityStore = useCityStore();
const stationStore = useStationStore();
const bicycleStore = useBikeStore();

const activeTab = ref<'cities' | 'stations' | 'bicycles' | 'slots' | 'redistribution' | 'maintenances'>('cities');
const selectedStationForSlots = ref<number | null>(null);

const loading = computed(() => {
  return cityStore.loading || stationStore.loading || bicycleStore.loading;
});

function handleViewSlots(stationId: number) {
  selectedStationForSlots.value = stationId;
  activeTab.value = 'slots';
}

onMounted(async () => {
  await Promise.all([
    cityStore.fetchCities(),
    stationStore.fetchStations(),
    stationStore.fetchSlots(),
    bicycleStore.fetchBicycles(),
  ]);
});
</script>

<style scoped lang="scss">
.admin-management-view {
  min-height: calc(100vh - 64px);
  background: var(--color-gray-very-light);
  padding: 2rem;
  margin-top: 64px; // Altura del header fijo
}

.management-container {
  background: var(--color-background-light);
  border-radius: 16px;
  max-width: 1400px;
  margin: 0 auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.management-header {
  padding: 2rem;
  background: var(--color-primary-light);
  color: var(--color-white);
  border-radius: 16px 16px 0 0;

  h2 {
    margin: 0;
    font-size: 1.75rem;
    font-weight: 700;
  }
}

.management-body {
  padding: 2rem;
}

.tabs {
  display: flex;
  gap: 1.25rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.tab {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.85rem 1.5rem;
  border-radius: 12px;
  border: 2px solid var(--color-border-light);
  background: var(--color-background-light);
  color: var(--color-text-primary-light);
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;

  .material-symbols-outlined {
    font-size: 1.25rem;
  }

  &:hover {
    border-color: var(--color-primary-light);
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  }

  &.active {
    background: var(--color-primary-light);
    color: var(--color-white);
    border-color: var(--color-primary-light);
    box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
  }
}

.loading {
  text-align: center;
  padding: 3rem;
  color: var(--color-gray-medium);
  font-size: 1.125rem;
  font-weight: 500;
}

// Dark mode
html[data-theme="dark"] {
  .admin-management-view {
    background: var(--color-background-dark);
  }

  .management-container {
    background: var(--color-surface-dark);
  }

  .tab {
    background: var(--color-background-dark);
    color: var(--color-text-primary-dark);
    border-color: var(--color-border-dark);

    &.active {
      background: var(--color-primary-dark);
      color: var(--color-white);
      border-color: var(--color-primary-dark);
    }
  }
}

@media (max-width: 768px) {
  .admin-management-view {
    padding: 1rem;
    margin-top: 120px; // Header más alto en móvil
  }

  .management-header {
    padding: 1.5rem;

    h2 {
      font-size: 1.5rem;
    }
  }

  .management-body {
    padding: 1rem;
  }

  .tabs {
    gap: 0.5rem;
  }

  .tab {
    padding: 0.5rem 1rem;
    font-size: 0.875rem;

    .material-symbols-outlined {
      font-size: 1rem;
    }
  }
}
</style>