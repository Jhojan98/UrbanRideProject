<template>
    <div class="dashboard-layout page-content">
        <h2 class="dashboard-title">{{$t('dashboard.stations.title')}}</h2>
        <div class="dashboard-grid">
            <div class="grid-left">
                <StationInfo
                    :stations="stationsList"
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
import { ref, computed, onMounted } from 'vue'
import StationInfo from '@/components/dashboard/StationInfo.vue'
import BikeInfo from '@/components/dashboard/BikeInfo.vue'
import MapComponent from '@/components/dashboard/MapComponent.vue'
import {
    Station,
    crearBicicleta,
    crearEstacion,
    bikeFlyweightFactory,
    stationFlyweightFactory
} from '@/patterns/flyweight'

// Datos mock usando el patrón Flyweight
// Use an untyped array for internal storage to avoid structural mismatch with private members,
// and expose a typed computed for components that expect Station[]
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const stations = ref<any[]>([])
const stationsList = computed<Station[]>(() => stations.value as Station[])

// Inicializar datos con el patrón Flyweight
onMounted(() => {
    console.log('🚀 Inicializando estaciones con patrón Flyweight...')

    // Main Station
    const station1 = crearEstacion(
        'ST-001',
        'Central Station',
        'Main Ave 123',
        'metro',
        true,
        false,
        true,
        [
            crearBicicleta('B-100', 'Optimal', 'UrbanX', 'electrica', 87),
            crearBicicleta('B-101', 'Optimal', 'UrbanLite', 'mecanica'),
            crearBicicleta('B-102', 'Needs maintenance', 'EcoRide', 'electrica', 55),
            crearBicicleta('B-103', 'Optimal', 'EcoRide', 'mecanica')
        ]
    )

    // Secondary Station
    const station2 = crearEstacion(
        'ST-002',
        'North Park',
        'North Street 45',
        'centro financiero',
        true,
        true,
        true,
        [
            crearBicicleta('B-200', 'Optimal', 'UrbanX', 'electrica', 63),
            crearBicicleta('B-201', 'Optimal', 'UrbanLite', 'mecanica')
        ]
    )

    // Small Station
    const station3 = crearEstacion(
        'ST-003',
        'South Terminal',
        'South Ave 800',
        'residencial',
        false,
        false,
        true,
        []
    )

    stations.value = [station1, station2, station3]

    // Mostrar estadísticas de optimización
    console.log('📊 Estadísticas de Flyweight:')
    console.log(`   🚲 Flyweights de bicicletas creados: ${bikeFlyweightFactory.getFlyweightCount()}`)
    console.log(`   🏢 Flyweights de estaciones creados: ${stationFlyweightFactory.getFlyweightCount()}`)
    bikeFlyweightFactory.listFlyweights()
})

const selectedStationId = ref<string | null>(null)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const selectedStation = computed(() => stations.value.find((s: any) => s.id === selectedStationId.value) || null)

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