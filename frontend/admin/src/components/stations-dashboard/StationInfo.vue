<template>
    <div class="station-info">
        <table class="dashboard-table">
            <thead>
                <tr>
                    <th>{{ t('dashboard.stations.nameC') }}</th>
                    <th>{{ t('dashboard.stations.locationC') }}</th>
                    <th>{{ t('dashboard.stations.CCTVC') }}</th>
                    <th>{{ t('dashboard.stations.iluminationC') }}</th>
                    <th>{{ t('dashboard.stations.slotsAvailableC') }}</th>
                    <th>{{ t('dashboard.stations.panicC') }}</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="station in props.stations" :key="station.idStation">
                    <td>{{ station.nameStation || station.stationName || 'N/A' }}</td>
                    <td>{{ station.latitude?.toFixed(4) || 'N/A' }}, {{ (station.longitude ?? station.length)?.toFixed(4) || 'N/A' }}</td>
                    <td>
                        <span v-if="station.cctvStatus" class="status-pill on">{{ t('dashboard.stations.statusActive', 0) }}</span>
                        <span v-else class="status-pill off">{{ t('dashboard.stations.statusActive', 1) }}</span>
                    </td>
                    <td>
                        <span v-if="station.lightingStatus" class="status-pill on">{{ t('dashboard.stations.statusActiveF', 0) }}</span>
                        <span v-else class="status-pill off">{{ t('dashboard.stations.statusActiveF', 1) }}</span>
                    </td>
                    <td
                        class="bike-count-cell"
                        @click="emitShowBikes(station.idStation)"
                    >
                        <button class="btn-view-bikes">
                            <span class="material-symbols-outlined">visibility</span>
                        </button>
                    </td>
                    <td>
                        <span v-if="station.panicButtonStatus" class="status-pill on">{{ t('dashboard.stations.panicStatus', 0) }}</span>
                        <span v-else class="status-pill off">{{ t('dashboard.stations.panicStatus', 1) }}</span>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Station } from '@/models/Station'

const { t } = useI18n()
// eslint-disable-next-line no-undef
const props = defineProps<{ stations: Station[] }>()
// eslint-disable-next-line no-undef
const emit = defineEmits<{ (e: 'show-bikes', stationId: number): void }>()

const hoveredStation = ref<Station | null>(null)
const tooltipVisible = ref(false)

function emitShowBikes(id: number) {
    emit('show-bikes', id)
}
function showSlots(station: Station) {
    hoveredStation.value = station
    tooltipVisible.value = true
}
function hideSlots() {
    hoveredStation.value = null
    tooltipVisible.value = false
}

</script>

<style lang="scss" scoped>
.bike-count-cell { 
  position: relative; 
  text-align: center; 
}

.btn-view-bikes {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.6rem 0.7rem;
  background: var(--color-primary-light);
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;

  .material-symbols-outlined {
    font-size: 1.3rem;
  }

  .separator {
    margin: 0 0.25rem;
    opacity: 0.7;
  }

  &:hover {
    background: #1e5a1f;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(46, 125, 50, 0.3);
  }

  &:active {
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .btn-view-bikes {
    padding: 0.5rem 0.6rem;
    font-size: 0.85rem;

    .material-symbols-outlined {
      font-size: 1.1rem;
    }
  }
}

@media (max-width: 480px) {
  .btn-view-bikes {
    padding: 0.4rem 0.5rem;
    font-size: 0.8rem;

    .material-symbols-outlined {
      font-size: 1rem;
    }
  }
}
</style>
