<template>
    <div class="bike-info" v-if="station">
        <div class="bike-info-header">
            <h3>Bicicletas en: {{ station.nombre }}</h3>
            <p class="subtitle">Total: {{ station.bicicletas.length }} / 15</p>
        </div>
        <table class="dashboard-table bikes-table" v-if="station.bicicletas.length">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Condición</th>
                    <th>Modelo</th>
                    <th>Tipo</th>
                    <th>Batería</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="b in station.bicicletas" :key="b.id">
                    <td>{{ b.id }}</td>
                    <td>{{ b.condicion }}</td>
                    <td>{{ b.modelo }}</td>
                    <td>
                        <span :class="['type-pill', b.tipo]">{{ b.tipo === 'electrica' ? 'Eléctrica' : 'Mecánica' }}</span>
                    </td>
                    <td>
                        <template v-if="b.tipo === 'electrica'">
                            <div class="battery-wrapper" :title="`Batería ${b.bateria ?? 0}%`">
                                <div class="battery-bar">
                                    <div class="battery-fill" :style="{ width: `${b.bateria ?? 0}%` }" :class="batteryClass(b.bateria)"></div>
                                </div>
                                <span class="battery-text">{{ b.bateria }}%</span>
                            </div>
                        </template>
                        <template v-else>
                            <span class="no-battery">—</span>
                        </template>
                    </td>
                </tr>
            </tbody>
        </table>
        <div v-else class="empty-message">No hay bicicletas registradas en esta estación.</div>
    </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
interface Bike {
    id: string
    condicion: string
    modelo: string
    tipo: 'electrica' | 'mecanica'
    bateria?: number
}
interface Station {
    id: string
    nombre: string
    bicicletas: Bike[]
}

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