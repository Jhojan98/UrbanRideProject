<template>
    <div class="dashboard-layout page-content">
        <h2 class="dashboard-title">Panel de Estaciones</h2>
        <div class="dashboard-grid">
            <div class="grid-left">
                <StationInfo
                    :stations="stations"
                    @show-bikes="handleShowBikes"
                />
                <BikeInfo
                    v-if="selectedStation"
                    :station="selectedStation"
                    class="bike-info-wrapper"
                />
            </div>
            <div class="grid-right">
                <MapComponent />
            </div>
        </div>
    </div>
    
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import StationInfo from '@/components/dashboard/StationInfo.vue'
import BikeInfo from '@/components/dashboard/BikeInfo.vue'
import MapComponent from '@/components/dashboard/MapComponent.vue'

interface Bike {
    id: string
    condicion: 'Excelente' | 'Buena' | 'Regular' | 'Mala'
    modelo: string
    tipo: 'electrica' | 'mecanica'
    bateria?: number // solo si electrica
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

// Datos mock; en integración real vendrán de API
const stations = ref<Station[]>([
    {
        id: 'ST-001',
        nombre: 'Estación Central',
        ubicacion: 'Av. Principal 123',
        cctvActivo: true,
        botonPanicoActivo: false,
        iluminacionActiva: true,
        bicicletas: [
            { id: 'B-100', condicion: 'Excelente', modelo: 'UrbanX', tipo: 'electrica', bateria: 87 },
            { id: 'B-101', condicion: 'Buena', modelo: 'UrbanLite', tipo: 'mecanica' },
            { id: 'B-102', condicion: 'Regular', modelo: 'EcoRide', tipo: 'electrica', bateria: 55 },
            { id: 'B-103', condicion: 'Excelente', modelo: 'EcoRide', tipo: 'mecanica' }
        ]
    },
    {
        id: 'ST-002',
        nombre: 'Parque Norte',
        ubicacion: 'Calle Norte 45',
        cctvActivo: true,
        botonPanicoActivo: true,
        iluminacionActiva: true,
        bicicletas: [
            { id: 'B-200', condicion: 'Buena', modelo: 'UrbanX', tipo: 'electrica', bateria: 63 },
            { id: 'B-201', condicion: 'Excelente', modelo: 'UrbanLite', tipo: 'mecanica' }
        ]
    },
    {
        id: 'ST-003',
        nombre: 'Terminal Sur',
        ubicacion: 'Av. Sur 800',
        cctvActivo: false,
        botonPanicoActivo: false,
        iluminacionActiva: true,
        bicicletas: []
    }
])

const selectedStationId = ref<string | null>(null)
const selectedStation = computed(() => stations.value.find(s => s.id === selectedStationId.value) || null)

function handleShowBikes(stationId: string) {
    selectedStationId.value = stationId
}
</script>
<style lang="scss" scoped>
.dashboard-title { margin: 0 0 1rem; }
.bike-info-wrapper { margin-top: 1rem; }
.dashboard-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
    align-items: start;
}
.grid-left { display: flex; flex-direction: column; }
.grid-right { min-height: 100%; }
@media (max-width: 1100px) {
    .dashboard-grid { grid-template-columns: 1fr; }
    .grid-right { order: -1; margin-bottom: 2rem; }
}
</style>