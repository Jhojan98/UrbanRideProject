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
                <tr v-for="station in props.stations" :key="station.id">
                    <td>{{ station.name }}</td>
                    <td>{{ station.location }}</td>
                    <td>
                        <span v-if="station.cctvActive" class="status-pill on">{{ t('dashboard.stations.statusActive', 0) }}</span>
                        <span v-else class="status-pill off">{{ t('dashboard.stations.statusActive', 1) }}</span>
                    </td>
                    <td>
                        <span v-if="station.lightingActive" class="status-pill on">{{ t('dashboard.stations.statusActiveF', 0) }}</span>
                        <span v-else class="status-pill off">{{ t('dashboard.stations.statusActiveF', 1) }}</span>
                    </td>
                    <td
                        class="bike-count-cell"
                        @click="emitShowBikes(station.id)"
                        @mouseenter="showSlots(station)"
                        @mouseleave="hideSlots"
                    >
                        <span class="clickable">{{ station.getCapacityStatus() }}</span>
                        <!-- Tooltip interno -->
                        <div
                            v-if="hoveredStation && hoveredStation.id === station.id && tooltipVisible"
                            class="slots-tooltip"
                        >
                            <p class="tooltip-title"><span class="material-symbols-outlined">{{ station.getIcon() }}</span> {{ station.category }} - Slots (1-{{ station.maxCapacity }})</p>
                            <div class="slots-grid">
                                <div
                                    v-for="n in station.maxCapacity"
                                    :key="n"
                                    :class="['slot-box', n <= station.bikes.length ? 'occupied' : 'empty']"
                                    :title="t('dashboard.stations.slotStatus', n <= station.bikes.length ? 0 : 1)"
                                >
                                    {{ n }}
                                </div>
                            </div>
                            <p class="summary">
                                Ocupados: {{ station.bikes.length }} | 
                                Libres: {{ station.maxCapacity - station.bikes.length }} |
                                Disponibles: {{ station.getAvailableBikes() }}
                            </p>
                        </div>
                    </td>
                    <td>
                        <span v-if="station.panicButtonActive" class="status-pill on">{{ t('dashboard.stations.panicStatus', 0) }}</span>
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
import type { Station } from '@/patterns/flyweight'

const { t } = useI18n()

// Declaraciones para que el analizador estático reconozca macros de Vue
declare function defineProps<T>(): T
declare function defineEmits<T>(): T

// eslint-disable-next-line
// @ts-ignore Vue macro provided by <script setup>
const props = defineProps<{ stations: Station[] }>()
const emit = defineEmits<{ (e: 'show-bikes', stationId: string): void }>()

const hoveredStation = ref<Station | null>(null)
const tooltipVisible = ref(false)

function emitShowBikes(id: string) {
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
.summary { margin:8px 0 0; font-size:11px; opacity:.85; }
.slots-grid { display:grid; grid-template-columns: repeat(5, 1fr); gap:6px; }
.slot-box { display:flex; align-items:center; justify-content:center; padding:5px 0; border-radius:6px; font-size:11px; font-weight:600; background: var(--color-gray-light); color: var(--color-text-primary-light); }
.slot-box.occupied { background:#b23a3a; color:#fff; }
.slot-box.empty { background:#2f7d2f; color:#fff; }
</style>