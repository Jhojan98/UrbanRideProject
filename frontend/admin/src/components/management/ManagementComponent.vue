<template>
  <div class="management-container">
    <h1 class="page-title">{{ t('management.title') }}</h1>

    <div class="tabs-container">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        :class="['tab-button', { active: activeTab === tab.id }]"
        @click="activeTab = tab.id"
      >
        <span class="material-symbols-outlined">{{ tab.icon }}</span>
        {{ t(tab.label) }}
      </button>
    </div>

    <div class="tab-content">
      <CitiesManagement v-if="activeTab === 'cities'" />
      <StationsManagement
        v-else-if="activeTab === 'stations'"
        @view-slots="handleViewSlots"
      />
      <BicyclesManagement v-else-if="activeTab === 'bicycles'" />
      <SlotsManagement
        v-else-if="activeTab === 'slots'"
        :initial-station-id="selectedStationForSlots"
      />
      <ComplaintsManagement v-else-if="activeTab === 'complaints'" />
    </div>

    <!-- Loading overlay -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="spinner"></div>
      <p>{{ t('common.loading') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useCityStore } from '@/stores/cityStore';
import { useStationStore } from '@/stores/stationStore';
import { useBikeStore } from '@/stores/bikeStore';
import { useComplaintsStore } from '@/stores/complaintsStore';
import CitiesManagement from './CitiesManagement.vue';
import StationsManagement from './StationsManagement.vue';
import BicyclesManagement from './BicyclesManagement.vue';
import SlotsManagement from './SlotsManagement.vue';
import ComplaintsManagement from './ComplaintsManagement.vue';

const { t } = useI18n();
const cityStore = useCityStore();
const stationStore = useStationStore();
const bicycleStore = useBikeStore();
const complaintsStore = useComplaintsStore();

const activeTab = ref('cities');
const selectedStationForSlots = ref<number | null>(null);

const tabs = [
  { id: 'cities', label: 'management.tabs.cities', icon: 'location_city' },
  { id: 'stations', label: 'management.tabs.stations', icon: 'store' },
  { id: 'bicycles', label: 'management.tabs.bicycles', icon: 'pedal_bike' },
  { id: 'slots', label: 'management.tabs.slots', icon: 'grid_view' },
  { id: 'complaints', label: 'management.tabs.complaints', icon: 'report_problem' },
];

const isLoading = computed(() => {
  return cityStore.loading || stationStore.loading || bicycleStore.loading || complaintsStore.loading;
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
    complaintsStore.fetchComplaints(),
  ]);
});
</script>

<style lang="scss" scoped>
.management-container {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 2rem;
  margin-bottom: 2rem;
  color: var(--color-text);
}

.tabs-container {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 2px solid var(--color-border);
}

.tab-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  background: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 1rem;
  color: var(--color-text-secondary);
  transition: all 0.3s;

  &:hover {
    color: var(--color-primary);
    background: var(--color-background-soft);
  }

  &.active {
    color: var(--color-primary);
    border-bottom-color: var(--color-primary);
    font-weight: 600;
  }

  .material-symbols-outlined {
    font-size: 1.5rem;
  }
}

.tab-content {
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 2000;

  .spinner {
    width: 50px;
    height: 50px;
    border: 4px solid var(--color-border);
    border-top-color: var(--color-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  p {
    margin-top: 1rem;
    color: var(--color-text);
    font-weight: 500;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
