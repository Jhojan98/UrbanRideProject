<template>
    <div class="station-info">
        <table class="dashboard-table">
            <thead>
                <tr>
                    <th>{{ t('dashboard.stations.nameC') }}</th>
                    <th>{{ t('dashboard.stations.locationC') }}</th>
                    <th>{{ t('dashboard.stations.CCTVC') }}</th>
                    <th>{{ t('dashboard.stations.iluminationC') }}</th>
                    <th>{{ t('dashboard.bikes.title').split(':')[0] }}</th>
                    <th>{{ t('dashboard.stations.panicC') }}</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="station in props.stations" :key="station.idStation">
                    <td>{{ station.nameStation }}</td>
                    <td>{{ station.latitude }}, {{ station.longitude }}</td>
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
                        @mouseenter="showSlots(station)"
                        @mouseleave="hideSlots"
                    >
                        <span class="clickable">{{ station.availableSlots }} / {{ station.totalSlots }}</span>
                        <!-- Tooltip interno -->
                        <div
                            v-if="hoveredStation && hoveredStation.idStation === station.idStation && tooltipVisible"
                            class="slots-tooltip"
                        >
                            <p class="tooltip-title">{{ t('dashboard.stations.tooltip.title', { name: station.nameStation }) }}</p>
                            <div class="slots-info">
                                <p><strong>{{ t('dashboard.stations.tooltip.totalSlots') }}</strong> {{ station.totalSlots }}</p>
                                <p><strong>{{ t('dashboard.stations.tooltip.availableSlots') }}</strong> {{ station.availableSlots }}</p>
                                <p><strong>{{ t('dashboard.stations.tooltip.electric') }}</strong> {{ station.availableElectricBikes || 0 }}</p>
                                <p><strong>{{ t('dashboard.stations.tooltip.mechanic') }}</strong> {{ station.availableMechanicBikes || 0 }}</p>
                            </div>
                        </div>
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
.bike-count-cell { position: relative; text-align: center; }
.bike-count-cell .clickable {
  cursor: pointer;
  font-weight: 600;
  color: var(--color-primary-light);
  display: inline-block;
  min-width: 80px;
}
.bike-count-cell:hover .clickable { text-decoration: underline; }
.slots-tooltip {
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%);
    margin-top: 8px;
    width: 220px;
    background: var(--color-background-light);
    border: 1px solid var(--color-border-light);
    padding: 10px 12px;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.25);
    z-index: 15;
    font-size: 12px;
    color: var(--color-text-primary-light);
}
.tooltip-title { margin:0 0 6px; font-weight:600; font-size:13px; }
.summary {
    margin:8px 0 0;
    font-size:11px;
    opacity:.85;
    display: flex;
    flex-direction: column;
    gap: 4px;

    span {
        display: flex;
        align-items: center;
        gap: 4px;
    }
}
.slots-grid { display:grid; grid-template-columns: repeat(5, 1fr); gap:6px; }
.slot-box {
    display:flex;
    flex-direction: column;
    align-items:center;
    justify-content:center;
    padding:5px 2px;
    border-radius:6px;
    font-size:11px;
    font-weight:600;
    background: var(--color-gray-light);
    color: var(--color-text-primary-light);
    position: relative;
}
.slot-box.occupied { background:#16a34a; color:#fff; }
.slot-box.traveling { background:#f59e0b; color:#fff; }
.slot-box.reserved {
    background:#8b5cf6;
    color:#fff;
    border: 2px dashed #fff;
}
.slot-box.empty { background:#e5e7eb; color:#6b7280; }
.slot-number { font-size: 10px; }
.slot-lock {
    font-size: 12px;
    margin-top: 2px;
}
</style>
