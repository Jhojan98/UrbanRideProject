<template>
    <div class="station-info">
        <table class="dashboard-table">
            <thead>
                <tr>
                    <th>Nombre</th>
                    <th>Ubicación</th>
                    <th>CCTV</th>
                    <th>Iluminación</th>
                    <th>Bicicletas (máx 15)</th>
                    <th>Botón Pánico</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="station in props.stations" :key="station.id">
                    <td>{{ station.nombre }}</td>
                    <td>{{ station.ubicacion }}</td>
                    <td>
                        <span :class="['status-pill', station.cctvActivo ? 'on' : 'off']">{{ station.cctvActivo ? 'Activo' : 'Inactivo' }}</span>
                    </td>
                    <td>
                        <span :class="['status-pill', station.iluminacionActiva ? 'on' : 'off']">{{ station.iluminacionActiva ? 'Activa' : 'Inactiva' }}</span>
                    </td>
                    <td
                        class="bike-count-cell"
                        @click="emitShowBikes(station.id)"
                        @mouseenter="showSlots(station)"
                        @mouseleave="hideSlots"
                    >
                        <span class="clickable">{{ station.bicicletas.length }}</span>
                        <!-- Tooltip interno restaurado -->
                        <div
                            v-if="hoveredStation && hoveredStation.id === station.id && tooltipVisible"
                            class="slots-tooltip"
                        >
                            <p class="tooltip-title">Slots (1-15)</p>
                            <div class="slots-grid">
                                <div
                                    v-for="n in 15"
                                    :key="n"
                                    :class="['slot-box', n <= station.bicicletas.length ? 'occupied' : 'empty']"
                                    :title="n <= station.bicicletas.length ? 'Ocupado' : 'Libre'"
                                >
                                    {{ n }}
                                </div>
                            </div>
                            <p class="summary">Ocupados: {{ station.bicicletas.length }} | Libres: {{ 15 - station.bicicletas.length }}</p>
                        </div>
                    </td>
                    <td>
                        <span :class="['status-pill', station.botonPanicoActivo ? 'on' : 'off']">{{ station.botonPanicoActivo ? 'Activo' : 'Inactivo' }}</span>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'

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
    ubicacion: string
    cctvActivo: boolean
    botonPanicoActivo: boolean
    iluminacionActiva: boolean
    bicicletas: Bike[]
}

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
.bike-count-cell .clickable { cursor: pointer; font-weight:600; color: var(--color-primary-light); }
.bike-count-cell:hover .clickable { text-decoration: underline; }
.bike-count-cell { position: relative; }
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