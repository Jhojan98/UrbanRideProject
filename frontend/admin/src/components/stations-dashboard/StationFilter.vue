<template>
  <div class="station-filter">
    <div class="filter-container">
      <label class="filter-label">
        <span class="material-symbols-outlined">filter_list</span>
        {{ t('dashboard.stations.filter.label') }}
      </label>
      <select
        v-model="selectedCity"
        @change="handleFilterChange"
        class="filter-select"
      >
        <option :value="null">{{ t('dashboard.stations.filter.all') }}</option>
        <option
          v-for="city in cities"
          :key="city.idCity"
          :value="city.idCity"
        >
          {{ city.cityName }}
        </option>
      </select>
      <div class="filter-info">
        <span v-if="selectedCity">
          {{ t('dashboard.stations.filter.showing', { filtered: filteredCount, total: totalCount }) }}
        </span>
        <span v-else>
          {{ t('dashboard.stations.filter.showingAll', { total: totalCount }) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/* eslint-disable no-undef */
import { ref, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useCityStore } from '@/stores/cityStore';

const { t } = useI18n();
const cityStore = useCityStore();

const _props = defineProps<{
  totalCount: number;
  filteredCount: number;
}>();

const emit = defineEmits<{
  (e: 'filter-change', cityId: number | null): void;
}>();

const selectedCity = ref<number | null>(null);

const cities = computed(() => cityStore.cities);

function handleFilterChange() {
  emit('filter-change', selectedCity.value);
}

onMounted(async () => {
  await cityStore.fetchCities();
});
</script>

<style lang="scss" scoped>
.station-filter {
  background: var(--color-background-light);
  padding: 1rem 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  margin-bottom: 1.5rem;
}

.filter-container {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.filter-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: var(--color-text-primary-light);
  font-size: 0.95rem;

  .material-symbols-outlined {
    font-size: 1.25rem;
    color: var(--color-primary-light);
  }
}

.filter-select {
  padding: 0.6rem 1rem;
  border: 2px solid var(--color-border-light);
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--color-text-primary-light);
  background: var(--color-background-light);
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 200px;

  &:hover {
    border-color: var(--color-primary-light);
  }

  &:focus {
    outline: none;
    border-color: var(--color-primary-light);
    box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.1);
  }
}

.filter-info {
  margin-left: auto;
  padding: 0.4rem 1rem;
  background: var(--color-gray-very-light);
  border-radius: 16px;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--color-text-secondary-light);
}

// Dark mode
html[data-theme="dark"] {
  .station-filter {
    background: var(--color-surface-dark);
  }

  .filter-label {
    color: var(--color-text-primary-dark);
  }

  .filter-select {
    background: var(--color-background-dark);
    color: var(--color-text-primary-dark);
    border-color: var(--color-border-dark);
  }

  .filter-info {
    background: var(--color-background-dark);
    color: var(--color-text-secondary-dark);
  }
}

@media (max-width: 768px) {
  .station-filter {
    padding: 0.75rem 1rem;
    margin-bottom: 1rem;
  }

  .filter-container {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
  }

  .filter-label {
    font-size: 0.9rem;

    .material-symbols-outlined {
      font-size: 1.1rem;
    }
  }

  .filter-select {
    width: 100%;
    padding: 0.5rem 0.75rem;
    font-size: 0.9rem;
    min-width: unset;
  }

  .filter-info {
    margin-left: 0;
    text-align: center;
    padding: 0.35rem 0.75rem;
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .station-filter {
    padding: 0.6rem 0.75rem;
    margin-bottom: 0.75rem;
  }

  .filter-container {
    gap: 0.6rem;
  }

  .filter-label {
    font-size: 0.85rem;

    .material-symbols-outlined {
      font-size: 1rem;
    }
  }

  .filter-select {
    padding: 0.45rem 0.6rem;
    font-size: 0.85rem;
  }

  .filter-info {
    padding: 0.3rem 0.6rem;
    font-size: 0.75rem;
  }
}
</style>
