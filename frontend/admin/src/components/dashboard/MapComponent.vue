<template>
    <div class="map-wrapper">
        <div id="map" class="map-container" />
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import L from 'leaflet'
import type { Map as LeafletMap, Marker } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import type { Station } from '@/patterns/flyweight'

// Props
interface Props {
    stations?: Station[]
}

// eslint-disable-next-line
const props = defineProps<Props>()

const map = ref<LeafletMap | null>(null)
const markers = ref<Marker[]>([])

function clearMarkers() {
    markers.value.forEach(m => m.remove())
    markers.value = []
}

function updateMarkers() {
    // Verificar que el mapa existe y está montado correctamente
    if (!map.value || !map.value.getContainer()) {
        console.log('⚠️ Mapa no disponible o no montado correctamente')
        return
    }

    if (!props.stations || props.stations.length === 0) {
        console.log('⚠️ No hay estaciones para mostrar')
        return
    }

    console.log('🗺️ Actualizando marcadores con', props.stations.length, 'estaciones')

    clearMarkers()

    // Crear iconos personalizados para estaciones
    const stationIcon = L.divIcon({
        className: 'station-marker',
        html: '<div style="background-color: #2563eb; width: 30px; height: 30px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>',
        iconSize: [30, 30],
        iconAnchor: [15, 15]
    })

    const bikeIcon = L.divIcon({
        className: 'bike-marker',
        html: '<div style="background-color: #10b981; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white; box-shadow: 0 1px 3px rgba(0,0,0,0.3);"></div>',
        iconSize: [20, 20],
        iconAnchor: [10, 10]
    })

    try {
        // Agregar marcadores de estaciones
        props.stations.forEach((station) => {
            if (!map.value) return

            const popup = L.popup({
                closeButton: true,
                autoClose: false,
                closeOnClick: false,
                className: 'custom-popup',
                minWidth: 200
            }).setContent(`
                <div style="min-width: 200px;">
                    <h3 style="margin: 0 0 8px 0; color: #1f2937;">${station.name}</h3>
                    <p style="margin: 4px 0; color: #6b7280; font-size: 12px;">${station.location}</p>
                    <p style="margin: 4px 0;"><strong>Categoría:</strong> ${station.category}</p>
                    <p style="margin: 4px 0;"><strong>Capacidad:</strong> ${station.getCapacityStatus()}</p>
                    <p style="margin: 4px 0;"><strong>🔒 Estacionadas:</strong> ${station.getLockedBikes()}</p>
                    <p style="margin: 4px 0;"><strong>🔓 En viaje:</strong> ${station.getTravelingBikes()}</p>
                                        <p style="margin: 4px 0;"><strong>📍 Reservados:</strong> ${station.getReservedSlots()}</p>
                    <p style="margin: 4px 0;"><strong>✅ Disponibles:</strong> ${station.getAvailableBikes()}</p>
                    <p style="margin: 4px 0;"><strong>CCTV:</strong> ${station.cctvActive ? '✅' : '❌'}</p>
                    <p style="margin: 4px 0;"><strong>Iluminación:</strong> ${station.lightingActive ? '✅' : '❌'}</p>
                </div>
            `)

            const stationMarker = L.marker([station.latitude, station.longitude], { icon: stationIcon })
                .addTo(map.value)
                .bindPopup(popup)

            // Prevenir errores al cerrar el popup
            stationMarker.on('popupclose', (e) => {
                try {
                    if (e.popup && e.popup._map) {
                        e.popup._map = null
                    }
                } catch (error) {
                    // Ignorar errores de cierre
                }
            })

            markers.value.push(stationMarker)

            // Agregar marcadores de bicicletas en la estación
            station.bikes.forEach((bike) => {
                if (!map.value) return

                const bikePopup = L.popup({
                    closeButton: true,
                    autoClose: false,
                    closeOnClick: false,
                    className: 'custom-popup',
                    minWidth: 150
                }).setContent(`
                    <div style="min-width: 150px;">
                        <h4 style="margin: 0 0 8px 0; color: #1f2937;">${bike.model} ${bike.getIcon()}</h4>
                        <p style="margin: 4px 0;"><strong>ID:</strong> ${bike.id}</p>
                        <p style="margin: 4px 0;"><strong>Estado del candado:</strong> ${bike.getLockIcon()} ${bike.getLockStatus()}</p>
                        <p style="margin: 4px 0;"><strong>Tipo:</strong> ${bike.type === 'electric' ? 'Eléctrica' : 'Mecánica'}</p>
                        <p style="margin: 4px 0;"><strong>Condición:</strong> ${bike.condition === 'Optimal' ? 'Óptimo' : 'Mantenimiento'}</p>
                        ${bike.battery !== undefined ? `<p style="margin: 4px 0;"><strong>Batería:</strong> ${bike.battery}%</p>` : ''}
                        <p style="margin: 4px 0; color: #6b7280; font-size: 11px;">Estación: ${station.name}</p>
                    </div>
                `)

                const bikeMarker = L.marker([bike.latitude, bike.longitude], { icon: bikeIcon })
                    .addTo(map.value)
                    .bindPopup(bikePopup)

                // Prevenir errores al cerrar el popup
                bikeMarker.on('popupclose', (e) => {
                    try {
                        if (e.popup && e.popup._map) {
                            e.popup._map = null
                        }
                    } catch (error) {
                        // Ignorar errores de cierre
                    }
                })

                markers.value.push(bikeMarker)
            })
        })
    } catch (error) {
        console.error('Error al actualizar marcadores:', error)
    }
}

onMounted(() => {
    // Centro inicial: Villavicencio (Meta, Colombia)
    const initialCenter: [number, number] = [4.1514, -73.6370]

    console.log('🗺️ Inicializando mapa de Villavicencio')

    // Usar nextTick para asegurar que el DOM está listo
    setTimeout(() => {
        try {
            map.value = L.map('map', {
                center: initialCenter,
                zoom: 13,
                zoomControl: true,
                preferCanvas: true,
                zoomAnimation: false, // Desactivar animación de zoom
                fadeAnimation: false, // Desactivar animación de fade
                markerZoomAnimation: false,
                doubleClickZoom: true // Mantener zoom por doble click pero sin animación
            })

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution:
                    '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
            }).addTo(map.value)

            // Cerrar popups antes de cualquier operación de zoom
            map.value.on('zoomstart', () => {
                try {
                    if (map.value && map.value._popup) {
                        map.value.closePopup()
                    }
                } catch (error) {
                    console.debug('Error al cerrar popup en zoomstart:', error)
                }
            })

            // Manejar doble click de forma segura
            map.value.on('dblclick', () => {
                try {
                    if (map.value && map.value._popup) {
                        map.value.closePopup()
                    }
                } catch (error) {
                    console.debug('Error al cerrar popup en dblclick:', error)
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

            // Actualizar marcadores con las estaciones reales después de que el mapa esté listo
            map.value.whenReady(() => {
                console.log('✅ Mapa listo para marcadores')
                updateMarkers()
            })
        } catch (error) {
            console.error('Error al inicializar el mapa:', error)
        }
    }, 100)
})

// Watch para actualizar marcadores cuando cambien las estaciones
watch(() => props.stations, (newStations) => {
    if (newStations && newStations.length > 0 && map.value) {
        console.log('📍 Estaciones actualizadas, refrescando marcadores')
        // Usar setTimeout para evitar problemas de sincronización
        setTimeout(() => {
            updateMarkers()
        }, 100)
    }
}, { deep: true })

onUnmounted(() => {
    console.log('🗑️ Limpiando mapa')
    if (map.value) {
        try {
            // Cerrar cualquier popup abierto
            map.value.closePopup()
            // Limpiar marcadores
            clearMarkers()
            // Remover todos los event listeners
            map.value.off()
            // Remover el mapa
            map.value.remove()
        } catch (error) {
            console.error('Error al limpiar el mapa:', error)
        } finally {
            map.value = null
        }
    }
})
</script>

<style scoped>
.map-wrapper {
    width: 100%;
    height: 100%;
}

.map-container {
    width: 100%;
    /* Ocupa viewport menos header/footer aprox. */
    min-height: calc(100vh - 140px);
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

@media (max-width: 768px) {
    .map-container {
        min-height: 50vh;
    }
}
</style>
