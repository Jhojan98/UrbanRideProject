<template>
    <div class="bike-info" v-if="station">
        <div class="bike-info-header">
            <h3><span class="material-symbols-outlined">{{ station.getIcon() }}</span> {{ t('dashboard.bikes.title', { stationName: station.name }) }}</h3>
            <p class="subtitle">{{ station.getCapacityStatus() }} - {{ station.category }}</p>
        </div>
        <table class="dashboard-table bikes-table" v-if="station.bikes.length">
            <thead>
                <tr>
                    <th>{{ t('dashboard.bikes.idC') }}</th>
                    <th>{{ t('dashboard.bikes.conditionC') }}</th>
                    <th>{{ t('dashboard.bikes.modelC') }}</th>
                    <th>{{ t('dashboard.bikes.typeC') }}</th>
                    <th>{{ t('dashboard.bikes.batteryC') }}</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="b in station.bikes" :key="b.id">
                    <td>{{ b.id }}</td>
                    <td>
                        <span v-if="b.condition === 'Optimal'">{{ t('dashboard.bikes.bikeCondition', 0) }}</span>
                        <span v-else>{{ t('dashboard.bikes.bikeCondition', 1) }}</span>
                    </td>
                    <td>{{ b.model }}</td>
                    <td>
                        <span v-if="b.type === 'electric'" class="type-pill electric">
                            {{ t('dashboard.bikes.bikeType', 0) }}
                        </span>
                        <span v-else class="type-pill mechanical">
                            {{ t('dashboard.bikes.bikeType', 1) }}
                        </span>
                    </td>
                    <td>
                        <template v-if="b.type === 'electric'">
                            <div class="battery-wrapper" :title="`${t('dashboard.bikes.batteryC')} ${b.battery ?? 0}%`">
                                <div class="battery-bar">
                                    <div class="battery-fill" :style="{ width: `${b.battery ?? 0}%` }" :class="batteryClass(b.battery)"></div>
                                </div>
                                <span class="battery-text">{{ b.battery }}%</span>
                            </div>
                        </template>
                        <template v-else>
                            <span class="no-battery">—</span>
                        </template>
                    </td>
                </tr>
            </tbody>
        </table>
        <div v-else class="empty-message">{{ t('dashboard.isEmpty') }}</div>
    </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Station } from '@/patterns/flyweight'

const { t } = useI18n()

// Declaraciones macros (para entorno analizador)
declare function defineProps<T>(): T

const props = defineProps<{ station: Station | null }>()
const station = computed(() => props.station)

function batteryClass(value?: number) {
    if (value == null) return ''
    if (value >= 70) return 'high'
    if (value >= 40) return 'medium'
    return 'low'
}
</script>
<style lang="scss" scoped>
.bike-info { margin-top: 1rem; }
.bike-info-header { display: flex; align-items: baseline; gap: 1rem; }
.bike-info-header h3 { margin: 0; }
.subtitle { margin: 0; font-size: .75rem; letter-spacing: .5px; text-transform: uppercase; color: var(--color-text-secondary-light); }
</style>