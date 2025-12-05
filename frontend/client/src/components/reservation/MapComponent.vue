<template>
  <div class="map-wrapper">
    <div id="map" :class="['map-container', { 'map-disabled': showAuthModal }]"></div>

    <!-- Modal: requiere iniciar sesión para acceder al mapa -->
    <div v-if="showAuthModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-content">
          <div class="success-icon">🔒</div>
          <h3>{{ $t('reservation.map.authRequired') }}</h3>
          <p>{{ $t('reservation.map.authMessage') }}</p>
          <div style="display:flex;gap:12px;justify-content:center;margin-top:18px;">
            <button class="butn-primary" @click="goToLogin">{{ $t('reservation.map.accept') }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Overlay: solo muestra info cuando hay ruta seleccionada (origen + destino) -->
    <div v-if="originPlain && destinationPlain" class="map-overlay">
      <div class="overlay-section">
        <h4>📍 {{ $t('reservation.map.originStation') }}</h4>
        <div>
          <div class="row"><strong>{{ originPlain.nameStation ?? '—' }}</strong></div>
          <div class="row">🚲 {{ originPlain.availableSlots ?? '—' }} {{ $t('reservation.map.availableBikes') }}</div>
        </div>
      </div>

      <div class="overlay-section">
        <h4>🅿️ {{ $t('reservation.map.destinationStation') }}</h4>
        <div>
          <div class="row"><strong>{{ destinationPlain.nameStation ?? '—' }}</strong></div>
          <div class="row">📍 {{ destinationPlain.availableSlots ?? '—' }} {{ $t('reservation.map.availableSlots') }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, defineProps, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import L from 'leaflet'
import type { Map as LeafletMap, Marker, Polyline } from 'leaflet'
import 'leaflet/dist/leaflet.css'
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import markerIcon from 'leaflet/dist/images/marker-icon.png'
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import markerShadow from 'leaflet/dist/images/marker-shadow.png'

// Arreglar íconos por defecto
// Eliminar método interno de iconos por defecto con cast seguro
delete (L.Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow
})

import { BicycleFactory } from '@/patterns/BicycleFlyweight'
import { BicycleWebSocketService } from '@/services/BicycleWebSocketService'
import { StationFactory } from '@/patterns/StationFlyweight'
import { StationWebSocketService } from '@/services/StationWebSocketService'
import { useStationStore } from '@/stores/station'
import type { Station } from '@/models/Station'

// Type used by WS and factories when payload shape may vary
type StationLike = Station | {
  id?: number
  idStation?: number
  name?: string
  nameStation?: string
  latitude: number
  longitude: number
  availableSlots?: number
  free_spots?: number
  type?: string
  cctvStatus?: boolean
  mechanical?: number
  electric?: number
  totalSlots?: number
}

// Interfaz para props de origen/destino
interface StationPoint {
  idStation?: number
  nameStation?: string
  latitude: number
  longitude: number
  availableSlots?: number
  type?: string
}

const props = defineProps<{
  origin?: StationPoint | null
  destination?: StationPoint | null
  useSockets?: boolean
  initialStations?: Station[]
}>()

// No longer emit station-click from the map; clicks only show info in the overlay

const { t: $t } = useI18n()
const stationStore = useStationStore()
const map = ref<LeafletMap | null>(null)
const isMounted = ref<boolean>(false)
const bicycleFactory = new BicycleFactory()
const wsService = new BicycleWebSocketService(bicycleFactory)
const stationFactory = new StationFactory()
const stationWsService = new StationWebSocketService(stationFactory)
// Flag para evitar re-renderizado redundante de estaciones
const stationsRendered = ref<boolean>(false)
interface ClickedStation {
  id: number | string
  name: string
  latitude: number
  longitude: number
  free_spots: number
  type: string
}
const clickedStation = ref<ClickedStation | null>(null)
const pendingTimeouts = new Set<number>()

// Auth modal: si no está autenticado, mostrar modal que obliga a iniciar sesión
const router = useRouter()
// const authStore = userAuth()
// const { token } = storeToRefs(authStore)
// Temporalmente deshabilitamos la restricción de login para facilitar
// la comprobación de la obtención/renderizado de estaciones.
// Antes: showAuthModal dependía de `token`; ahora forzamos `false`.
const showAuthModal = ref<boolean>(false)

// Comentado temporalmente: evitar que cambios en el token cierren/abran el modal
// watch(token, (val) => {
//   showAuthModal.value = !val
// })

function goToLogin() {
  router.push({ name: 'login' }).catch(() => void 0)
}

// Nota: no existe función de "cancelar" para evitar que el usuario interactúe sin iniciar sesión.

// Para marcadores de ruta (origen/destino)
let originMarker: Marker | null = null
let destMarker: Marker | null = null
let routeLine: Polyline | null = null

const originPlain = computed(() => props.origin ? {
  nameStation: props.origin.nameStation ?? 'Origen',
  availableSlots: props.origin.availableSlots ?? 0
} : null)

const destinationPlain = computed(() => props.destination ? {
  nameStation: props.destination.nameStation ?? 'Destino',
  availableSlots: props.destination.availableSlots ?? 0
} : null)

// Helper: registrar timeout para limpieza en onUnmounted
function setMountedTimeout(callback: () => void, delay: number): number {
  const timeoutId = window.setTimeout(() => {
    if (isMounted.value) {
      callback()
    }
    pendingTimeouts.delete(timeoutId)
  }, delay)
  pendingTimeouts.add(timeoutId)
  return timeoutId
}

// NOTE: always show all stations; selection will be done via dropdowns

function addOriginMarker(o: StationPoint) {
  if (!map.value || !isMounted.value) return
  if (originMarker) {
    originMarker.remove()
    originMarker = null
  }
  console.log('🔵 Agregando marcador ORIGEN en:', { lat: o.latitude, lng: o.longitude, nameStation: o.nameStation })

  // Usar icono estándar de Leaflet con color verde para origen
  const greenIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  })

  originMarker = L.marker([o.latitude, o.longitude], { icon: greenIcon }).addTo(map.value as LeafletMap)
  if (o.nameStation) {
    const popupText = `<strong>${$t('reservation.map.markerOrigin')}</strong><br/>${o.nameStation}<br/>${$t('reservation.map.markerCoords', { lat: o.latitude.toFixed(4), lng: o.longitude.toFixed(4) })}`
    originMarker.bindPopup(popupText)
  }
  setMountedTimeout(() => originMarker?.openPopup(), 200)
}

function addDestMarker(d: StationPoint) {
  if (!map.value || !isMounted.value) return
  if (destMarker) {
    destMarker.remove()
    destMarker = null
  }
  console.log('🔵 Agregando marcador DESTINO en:', { lat: d.latitude, lng: d.longitude, nameStation: d.nameStation })

  // Usar icono estándar de Leaflet con color rojo para destino
  const redIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  })

  destMarker = L.marker([d.latitude, d.longitude], { icon: redIcon }).addTo(map.value as LeafletMap)
  if (d.nameStation) {
    const popupText = `<strong>${$t('reservation.map.markerDestination')}</strong><br/>${d.nameStation}<br/>${$t('reservation.map.markerCoords', { lat: d.latitude.toFixed(4), lng: d.longitude.toFixed(4) })}`
    destMarker.bindPopup(popupText)
  }
  setMountedTimeout(() => destMarker?.openPopup(), 200)
}

function drawLine() {
  if (!map.value) return
  if (!props.origin || !props.destination) {
    if (routeLine) {
      routeLine.remove()
      routeLine = null
    }
    return
  }

  if (routeLine) {
    routeLine.remove()
    routeLine = null
  }

  try {
    routeLine = L.polyline(
      [[props.origin.latitude, props.origin.longitude], [props.destination.latitude, props.destination.longitude]],
      { color: 'blue', weight: 4, opacity: 0.85 }
    ).addTo(map.value as LeafletMap)
  } catch (error) {
    console.warn('[Map] Error drawing route line:', error)
    routeLine = null
  }
}

function fitMap() {
  if (!map.value || !isMounted.value) return

  try {
    if (props.origin && props.destination) {
      const bounds = L.latLngBounds([
        [props.origin.latitude, props.origin.longitude],
        [props.destination.latitude, props.destination.longitude],
      ])
      map.value.fitBounds(bounds, { padding: [40, 40], animate: false })
      return
    }

    if (props.origin && !props.destination) {
      map.value.setView([props.origin.latitude, props.origin.longitude], 14, { animate: false })
      return
    }

    if (!props.origin && props.destination) {
      map.value.setView([props.destination.latitude, props.destination.longitude], 14, { animate: false })
      return
    }
  } catch (error) {
    console.warn('[Map] Error fitting map bounds:', error)
  }
}

// Render stations from store on the map
function renderStationsFromStore() {
  const stations = stationStore.allStations

  if (!stations || stations.length === 0) {
    console.warn('[Map] No stations available in store')
    return false
  }

  console.log('[Map] Renderizando estaciones del store:', stations.length)

  stations.forEach(s => {
    const lat = s.latitude
    const lng = s.longitude
    const name = s.nameStation
    const mechanical = s.mechanical ?? 0
    const electric = s.electric ?? 0
    const cctvDisplay = typeof s.cctvStatus === 'boolean' ? (s.cctvStatus ? '✅ Activo' : '❌ Inactivo') : '-'

    // Log para cada estación
    console.log(`[Map] ${name}: ⚡ ${electric}, ⚙️ ${mechanical}`);

    // Determinar disponibilidad para viaje
    const canTravelMech = mechanical > 0 ? '✅' : '❌'
    const canTravelElec = electric > 0 ? '✅' : '❌'

    if (typeof lat === 'number' && typeof lng === 'number') {
      const mk = L.marker([lat, lng]).addTo(map.value as LeafletMap)
      const popup = `
        <div style="font-family: Arial, sans-serif; min-width: 200px;">
          <strong style="font-size: 14px; color: #2c3e50;">${name ?? 'Estación'}</strong>
          <div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid #e0e0e0;">
            <div style="margin: 4px 0;"><strong>Tipo:</strong> ${s.type ?? '-'}</div>
            <div style="margin: 4px 0;"><strong>CCTV:</strong> ${cctvDisplay}</div>
            <div style="margin: 4px 0;"><strong>Slots:</strong> ${s.availableSlots}/${s.totalSlots}</div>
          </div>
          <div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid #e0e0e0;">
            <div style="font-weight: bold; margin-bottom: 6px; color: #34495e;">🚲 Bicicletas Disponibles:</div>
            <div style="display: flex; justify-content: space-between; margin: 4px 0;">
              <span>⚙️ Mecánicas:</span>
              <span style="font-weight: bold; color: ${mechanical > 0 ? '#27ae60' : '#e74c3c'};">${mechanical} ${canTravelMech}</span>
            </div>
            <div style="display: flex; justify-content: space-between; margin: 4px 0;">
              <span>⚡ Eléctricas:</span>
              <span style="font-weight: bold; color: ${electric > 0 ? '#27ae60' : '#e74c3c'};">${electric} ${canTravelElec}</span>
            </div>
          </div>
        </div>
      `
      mk.bindPopup(popup)
      // on hover show popup
      mk.on('mouseover', () => mk.openPopup())
      mk.on('mouseout', () => mk.closePopup())
    }
  })

  stationsRendered.value = true
  console.log('[Map] stations rendered from store, count:', stations.length)
  return true
}

onMounted(() => {
  const initialCenter: [number, number] = [4.1514, -73.6370]

  map.value = L.map('map', {
    center: initialCenter,
    zoom: 13,
    zoomControl: true,
    // Desactivar todas las animaciones de zoom/transición para evitar
    // errores al desmontar o durante cambios rápidos.
    zoomAnimation: false,
    markerZoomAnimation: false,
    fadeAnimation: false,
    inertia: false,
  })

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    // Reducir trabajo durante zoom/redibujos
    updateWhenIdle: true,
    keepBuffer: 0,
  }).addTo(map.value as LeafletMap)

  // Fetch and render stations from store
  if (props.useSockets === false) {
    // Fetch stations from backend via store
    stationStore.fetchStations().then((stations) => {
      if (stations && stations.length > 0) {
        renderStationsFromStore()
      } else {
        console.warn('[Map] No stations loaded from store')
        if (props.initialStations && props.initialStations.length) {
          console.log('[Map] Using initialStations prop as fallback')
          props.initialStations.forEach(st => {
            const marker = stationFactory.getStationMarker(st as any)
            marker.render(map.value as LeafletMap)
          })
          stationsRendered.value = true
        }
      }
    }).catch((err) => {
      console.error('[Map] Error fetching stations from store:', err)
      if (props.initialStations && props.initialStations.length) {
        console.log('[Map] Using initialStations prop as fallback after error')
        props.initialStations.forEach(st => {
          const marker = stationFactory.getStationMarker(st as any)
          marker.render(map.value as LeafletMap)
        })
        stationsRendered.value = true
      }
    })
  } else {
    console.log('[Map] WebSocket mode enabled, waiting for WS data')
    stationsRendered.value = false
  }

  // Intentar conectar al WebSocket de estaciones y solicitar carga inicial.
  try {
    stationWsService.connect((stations, factory) => {
      // Bulk inicial recibido: renderizar todas las estaciones en el mapa
      try {
        stations.forEach((st: StationLike) => {
          const mk = factory.getStationMarker(st as unknown as StationLike)
          mk.setTranslator($t)
          mk.render(map.value as LeafletMap)
          mk.onClick((station: StationLike) => {
            const payload = {
              id: station.idStation ?? station.id,
              name: station.nameStation ?? station.name,
              latitude: station.latitude,
              longitude: station.longitude,
              free_spots: station.availableSlots ?? station.free_spots ?? 0,
              type: station.type ?? 'bike'
            }
            if (clickedStation.value && clickedStation.value.id === payload.id) clickedStation.value = null
            else clickedStation.value = payload
          })
        })
      } catch (e) { console.error('[Map] Error rendering bulk stations', e) }
    }, (station, factory) => {
      // Update incremental: render/update single station
      try {
        const mk = factory.getStationMarker(station as unknown as StationLike)
        mk.setTranslator($t)
        mk.render(map.value as LeafletMap)
        mk.onClick((st: StationLike) => {
          const payload = {
            id: st.idStation ?? st.id,
            name: st.nameStation ?? st.name,
            latitude: st.latitude,
            longitude: st.longitude,
            free_spots: st.availableSlots ?? st.free_spots ?? 0,
            type: st.type ?? 'bike'
          }
          if (clickedStation.value && clickedStation.value.id === payload.id) clickedStation.value = null
          else clickedStation.value = payload
        })
      } catch (e) { console.error('[Map] Error updating station', e) }
    })
  } catch (e) { console.warn('[Map] No se pudo conectar al Stations WS', e) }

  // Debug temporal: comprobar estado del WS y cantidad de estaciones almacenadas
  setTimeout(() => {
    try {
      console.log('[Map DEBUG] Stations WS connected?', stationWsService.getIsConnected());
      console.log('[Map DEBUG] Stations WS has initial bulk?', stationWsService.getHasInitialBulk());
      console.log('[Map DEBUG] Station factory size:', stationWsService.getFactory().size());
      console.log('[Map DEBUG] Stations cache count:', stationWsService.getStationCount());
      console.log('[Map DEBUG] Stations from service:', stationWsService.getStations());
    } catch (err) {
      console.warn('[Map DEBUG] Error leyendo estado Stations WS', err);
    }
  }, 2000);

  // Re-check más tarde por si la conexión tarda en establecerse
  setTimeout(() => {
    try {
      console.log('[Map DEBUG] (later) Stations WS connected?', stationWsService.getIsConnected());
      console.log('[Map DEBUG] (later) Stations WS has initial bulk?', stationWsService.getHasInitialBulk());
      console.log('[Map DEBUG] (later) Station factory size:', stationWsService.getFactory().size());
      console.log('[Map DEBUG] (later) Stations cache count:', stationWsService.getStationCount());
    } catch (err) { void err }
  }, 6000);
  // WebSocket de estaciones (fallback, no esencial)
  // stationWsService.connect(...) - comentado para evitar ruido

  // WebSocket de bicicletas (opcional)
  // wsService.connect(...) - comentado para evitar ruido

  // Marcar el componente como montado
  isMounted.value = true
})

onUnmounted(() => {
  // Marcar componente como desmontado inmediatamente
  isMounted.value = false

  // Limpiar todos los timeouts pendientes
  pendingTimeouts.forEach(timeoutId => clearTimeout(timeoutId))
  pendingTimeouts.clear()

  // Desconectar WebSocket si se conectó
  try { wsService.disconnect() } catch (e) { void e }
  try { stationWsService.disconnect() } catch (e) { void e }

  bicycleFactory.clear()
  stationFactory.clear()

  if (originMarker) { originMarker.remove(); originMarker = null }
  if (destMarker) { destMarker.remove(); destMarker = null }
  if (routeLine) { routeLine.remove(); routeLine = null }

  clickedStation.value = null

  if (map.value) {
    // Detener cualquier animación en progreso antes de eliminar el mapa
    try {
      map.value.stop()
    } catch (e) { void e }

    // Remover todos los event listeners
    try {
      map.value.off()
    } catch (e) { void e }

    // Finalmente remover el mapa
    try {
      map.value.remove()
    } catch (e) { void e }

    map.value = null
  }
})

// Watcher para cambios en hasRouteSelected (mostrar/ocultar estaciones)
// No automatic hiding of stations when a route is selected. Selection
// of origin/destination is handled via dropdowns per product requirement.

// Watchers para origen/destino (dibujar línea y ajustar vista)
watch(() => props.origin, () => {
  if (!map.value || !isMounted.value) return
  try {
    if (props.origin) {
      addOriginMarker(props.origin)
    } else if (originMarker) {
      originMarker.remove()
      originMarker = null
    }
    drawLine()
    fitMap()
  } catch (error) {
    console.warn('[Map] Error in origin watcher:', error)
  }
})

watch(() => props.destination, () => {
  if (!map.value || !isMounted.value) return
  try {
    if (props.destination) {
      addDestMarker(props.destination)
    } else if (destMarker) {
      destMarker.remove()
      destMarker = null
    }
    drawLine()
    fitMap()
  } catch (error) {
    console.warn('[Map] Error in destination watcher:', error)
  }
})

// Ensure line and view update whenever either endpoint changes together
watch(() => [props.origin, props.destination], () => {
  if (!map.value || !isMounted.value) return
  try {
    drawLine()
    fitMap()
  } catch (error) {
    console.warn('[Map] Error updating line and bounds:', error)
  }
})
</script>

<style scoped>
.map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.map-container {
  width: 100%;
  min-height: calc(100vh - 140px);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.map-disabled {
  opacity: 0.6;
  pointer-events: none;
}

.map-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 8px;
  padding: 10px;
  width: 260px;
  font-size: 13px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
  z-index: 1000;
}

.map-overlay h4 { margin: 0 0 6px 0; font-size: 13px; }
.overlay-section { margin-bottom: 8px; border-bottom: 1px solid #eee; padding-bottom: 6px; }
.overlay-section:last-child { border-bottom: none; margin-bottom: 0; padding-bottom: 0; }
.row { margin: 3px 0; }
.muted { color: #666; font-style: italic; font-size: 12px; }

@media (max-width: 768px) {
  .map-container {
    min-height: 50vh;
  }
}

/* Modal local: aseguramos que quede por encima del overlay de estación */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000; /* superior al z-index del overlay de estación */
}

.modal {
  background: white;
  border-radius: 12px;
  padding: 22px;
  max-width: 480px;
  width: 92%;
  text-align: center;
  box-shadow: 0 8px 30px rgba(0,0,0,0.18);
}

.modal-content h3 { margin: 0 0 8px 0; }

.success-icon { font-size: 36px; margin-bottom: 8px; }
</style>
