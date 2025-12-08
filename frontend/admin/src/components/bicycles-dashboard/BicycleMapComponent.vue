<template>
    <div class="bicycle-map-wrapper">
        <div id="bicycle-map" class="map-container" />
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import L from 'leaflet'
import type { Map as LeafletMap } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import type Bike from '@/models/Bike'
import { type BicycleFactory, type BicycleMarker } from '@/patterns/BicycleFlyweight'

// eslint-disable-next-line no-undef
const props = defineProps<{
    bikes: Bike[]
    factory: BicycleFactory
}>()

const map = ref<LeafletMap | null>(null)
const renderedMarkers = ref<Map<string, BicycleMarker>>(new Map())

function clearMarkers() {
    renderedMarkers.value.forEach(marker => marker.remove())
    renderedMarkers.value.clear()
}

function updateMarkers() {
    if (!map.value || !map.value.getContainer()) {
        console.log('⚠️ Mapa de bicicletas no disponible')
        return
    }

    if (!props.bikes || props.bikes.length === 0) {
        console.log('⚠️ No hay bicicletas para mostrar')
        clearMarkers()
        return
    }

    console.log('🚲 Actualizando marcadores con', props.bikes.length, 'bicicletas')

    try {
        // Crear un Set con los IDs de las bicicletas actuales
        const currentBikeIds = new Set(props.bikes.map(b => b.id))

        // Eliminar marcadores de bicicletas que ya no existen
        renderedMarkers.value.forEach((marker, bikeId) => {
            if (!currentBikeIds.has(bikeId)) {
                marker.remove()
                renderedMarkers.value.delete(bikeId)
            }
        })

        // Actualizar o crear marcadores
        props.bikes.forEach((bike) => {
            if (!map.value) return

            // Validar coordenadas
            if (bike.lat == null || bike.lon == null) {
                console.warn(`Bicicleta ${bike.id} sin coordenadas válidas`)
                return
            }

            // Obtener o crear el marcador usando el factory
            const bicycleMarker = props.factory.getBicycleMarker(bike)

            // Renderizar el marcador en el mapa
            bicycleMarker.render(map.value)

            // Guardar referencia
            renderedMarkers.value.set(bike.id, bicycleMarker)
        })

        console.log('✅ Marcadores de bicicletas actualizados:', renderedMarkers.value.size)

    } catch (error) {
        console.error('Error al actualizar marcadores de bicicletas:', error)
    }
}

onMounted(() => {
    // Centro inicial: Villavicencio (Meta, Colombia)
    const initialCenter: [number, number] = [4.1514, -73.6370]

    console.log('🗺️ Inicializando mapa de bicicletas')

    setTimeout(() => {
        try {
            map.value = L.map('bicycle-map', {
                center: initialCenter,
                zoom: 13,
                zoomControl: true,
                preferCanvas: true,
                zoomAnimation: false,
                fadeAnimation: false,
                markerZoomAnimation: false,
                doubleClickZoom: true
            })

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution:
                    '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
            }).addTo(map.value)

            // Cerrar popups antes de operaciones de zoom
            map.value.on('zoomstart', () => {
                try {
                    if (map.value && map.value._popup) {
                        map.value.closePopup()
                    }
                } catch (error) {
                    console.debug('Error al cerrar popup en zoomstart:', error)
                }
            })

            // Limpiar referencias cuando se cierra un popup
            map.value.on('popupclose', (e) => {
                try {
                    if (e.popup) {
                        e.popup._map = null
                        if (e.popup._source) {
                            e.popup._source = null
                        }
                    }
                } catch (error) {
                    console.debug('Error al limpiar popup:', error)
                }
            })

            // Actualizar marcadores después de que el mapa esté listo
            map.value.whenReady(() => {
                console.log('✅ Mapa de bicicletas listo')
                updateMarkers()
            })
        } catch (error) {
            console.error('Error al inicializar el mapa de bicicletas:', error)
        }
    }, 100)
})

// Watch para actualizar marcadores cuando cambien las bicicletas
watch(() => props.bikes, (newBikes) => {
    if (newBikes && newBikes.length > 0 && map.value) {
        console.log('📍 Bicicletas actualizadas, refrescando marcadores')
        setTimeout(() => {
            updateMarkers()
        }, 100)
    }
}, { deep: true })

onUnmounted(() => {
    console.log('🗑️ Limpiando mapa de bicicletas')
    if (map.value) {
        try {
            map.value.closePopup()
            clearMarkers()
            map.value.off()
            map.value.remove()
            map.value = null
        } catch (error) {
            console.error('Error al limpiar mapa:', error)
        }
    }
})
</script>

<style lang="scss" scoped>
.bicycle-map-wrapper {
    width: 100%;
    height: 100%;
    min-height: 600px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    border: 1px solid var(--color-border-light);
}

.map-container {
    width: 100%;
    height: 100%;
    min-height: 600px;
    border-radius: 8px;
}

:deep(.leaflet-popup-content-wrapper) {
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

:deep(.leaflet-popup-content) {
    margin: 12px;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

:deep(.bicycle-marker),
:deep(.bicycle-marker-low),
:deep(.bicycle-marker-mechanic),
:deep(.bicycle-marker-locked) {
    background: transparent;
    border: none;
}
</style>
