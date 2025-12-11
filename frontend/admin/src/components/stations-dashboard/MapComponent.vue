<template>
    <div class="map-wrapper">
        <div id="map" class="map-container" />
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import L from 'leaflet'
import type { Map as LeafletMap, Marker } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import type { Station } from '@/models/Station'

// eslint-disable-next-line no-undef
const props = defineProps<{ stations: Station[] }>()

const { t } = useI18n()

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

    // Función para crear iconos personalizados según el tipo de estación
    const createStationIcon = (type?: string) => {
        const normalizedType = type ? type.toLowerCase().replace(/\s+/g, '') : ''
        const typeStyles: Record<string, { color: string; icon: string }> = {
            metro: { color: '#1e88e5', icon: 'fa-subway' },
            financialcenter: { color: '#f59e0b', icon: 'fa-briefcase' },
            residential: { color: '#10b981', icon: 'fa-building' }
        }

        const style = typeStyles[normalizedType] || { color: '#2563eb', icon: 'fa-location-dot' }

        const html = `<div style="width:36px;height:36px;background:${style.color};border:3px solid #fff;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:20px;box-shadow:0 2px 6px rgba(0,0,0,.35);"><i class="fa ${style.icon}"></i></div>`
        
        return L.divIcon({ 
            html, 
            className: 'station-icon', 
            iconSize: [36, 36], 
            iconAnchor: [18, 36], 
            popupAnchor: [0, -36] 
        })
    }

    try {
        // Agregar marcadores de estaciones
        props.stations.forEach((station) => {
            if (!map.value) return

            // Obtener coordenadas usando campos flexibles
            const lat = station.latitude
            const lon = station.longitude ?? station.length

            if (lat == null || lon == null) {
                console.warn(`Estación ${station.nameStation || station.stationName} sin coordenadas válidas`, { lat, lon, station })
                return
            }

            const availableElectric = station.availableElectricBikes ?? 0
            const availableMechanical = station.availableMechanicBikes ?? 0

            const stationName = station.nameStation || station.stationName || 'N/A'

            const popupContent = `
                <div style="min-width: 220px;">
                    <h3 style="margin: 0 0 8px 0; color: #1f2937;">${stationName}</h3>
                    <p style="margin: 4px 0; color: #6b7280; font-size: 12px;">${lat.toFixed(4)}, ${lon.toFixed(4)}</p>
                    <p style="margin: 4px 0;"><strong>${t('dashboard.stations.map.slots')}</strong> ${station.availableSlots} / ${station.totalSlots}</p>
                    <p style="margin: 4px 0;"><strong>${t('dashboard.stations.map.electric')}</strong> ${availableElectric}</p>
                    <p style="margin: 4px 0;"><strong>${t('dashboard.stations.map.mechanic')}</strong> ${availableMechanical}</p>
                    <p style="margin: 4px 0;"><strong>${t('dashboard.stations.map.cctv')}</strong> ${station.cctvStatus ? '✅' : '❌'}</p>
                    <p style="margin: 4px 0;"><strong>${t('dashboard.stations.map.lighting')}</strong> ${station.lightingStatus ? '✅' : '❌'}</p>
                    <p style="margin: 4px 0;"><strong>${t('dashboard.stations.map.panic')}</strong> ${station.panicButtonStatus ? '✅' : '❌'}</p>
                </div>
            `

            const popup = L.popup({
                closeButton: true,
                autoClose: false,
                closeOnClick: false,
                className: 'custom-popup',
                minWidth: 200
            }).setContent(popupContent)

            const stationMarker = L.marker([lat, lon], { icon: createStationIcon(station.type) })
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
