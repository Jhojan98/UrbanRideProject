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
                    <td>{{ t('dashboard.bikes.bikeCondition', b.condition === 'Optimal' ? 0 : 1) }}</td>
                    <td>{{ b.modelo }}</td>
                    <td>
                        <span :class="['type-pill', b.tipo]">{{ b.getIcon() }} {{ t('dashboard.bikes.bikeType', b.tipo === 'electrica' ? 0 : 1) }}</span>
                    </td>
                    <td>
                        <template v-if="b.tipo === 'electrica'">
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
.bike-info-header h3 { margin: 0; display: flex; align-items: center; gap: 0.5rem; }
.subtitle { margin: 0; font-size: .75rem; letter-spacing: .5px; text-transform: uppercase; color: var(--color-text-secondary-light); }

/* Anchos fijos para columnas de tabla de bicicletas */
.bikes-table th:nth-child(1),
.bikes-table td:nth-child(1) { width: 12%; min-width: 80px; }
.bikes-table th:nth-child(2),
.bikes-table td:nth-child(2) { width: 20%; min-width: 140px; text-align: center; }
.bikes-table th:nth-child(3),
.bikes-table td:nth-child(3) { width: 18%; min-width: 100px; }
.bikes-table th:nth-child(4),
.bikes-table td:nth-child(4) { width: 20%; min-width: 130px; text-align: center; }
.bikes-table th:nth-child(5),
.bikes-table td:nth-child(5) { width: 30%; min-width: 150px; }

/* Estilos para la batería */
.battery-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}
.battery-bar {
  width: 80px;
  height: 18px;
  background: #e5e7eb;
  border-radius: 9px;
  overflow: hidden;
  position: relative;
  border: 1px solid #d1d5db;
}
.battery-fill {
  height: 100%;
  transition: width 0.3s ease;
  border-radius: 8px;
}
.battery-fill.high { background: #10b981; }
.battery-fill.medium { background: #f59e0b; }
.battery-fill.low { background: #ef4444; }
.battery-text {
  font-size: 0.75rem;
  font-weight: 600;
  min-width: 35px;
  text-align: right;
}
.no-battery {
  font-size: 1.2rem;
  color: var(--color-text-secondary-light);
}
</style>