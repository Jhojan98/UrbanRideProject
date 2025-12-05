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
          <div class="row"><strong>{{ originPlain.name ?? '—' }}</strong></div>
          <div class="row">🚲 {{ originPlain.free_spots ?? '—' }} {{ $t('reservation.map.availableBikes') }}</div>
        </div>
      </div>

      <div class="overlay-section">
        <h4>🅿️ {{ $t('reservation.map.destinationStation') }}</h4>
        <div>
          <div class="row"><strong>{{ destinationPlain.name ?? '—' }}</strong></div>
          <div class="row">📍 {{ destinationPlain.free_spots ?? '—' }} {{ $t('reservation.map.availableSlots') }}</div>
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

// Datos locales de prueba para estaciones (cuando WebSocket no está disponible)
interface StationLike {
  idStation?: number
  id?: number
  nameStation?: string
  name?: string
  latitude: number
  longitude: number
  availableSlots?: number
  free_spots?: number
  type?: string
}

const mockStations: StationLike[] = [
  { idStation: 10, nameStation: "Estación Centro", latitude: 4.1430, longitude: -73.6290, availableSlots: 5, totalSlots: 10, type: 'bike', mechanical: 3, electric: 2, slots: [] as unknown[] },
  { idStation: 11, nameStation: "Parque Sikuani", latitude: 4.1475, longitude: -73.6260, availableSlots: 2, totalSlots: 10, type: 'bike', mechanical: 1, electric: 1, slots: [] as unknown[] },
  { idStation: 12, nameStation: "Zona Universitaria", latitude: 4.1505, longitude: -73.6235, availableSlots: 0, totalSlots: 10, type: 'bike', mechanical: 0, electric: 0, slots: [] as unknown[] },
  { idStation: 1, nameStation: "Metro Estación Central", latitude: 4.1425, longitude: -73.6312, availableSlots: 5, totalSlots: 10, type: 'metro', mechanical: 0, electric: 0, slots: [] as unknown[] },
  { idStation: 2, nameStation: "Metro Sikuani", latitude: 4.1480, longitude: -73.6270, availableSlots: 2, totalSlots: 10, type: 'metro', mechanical: 0, electric: 0, slots: [] as unknown[] }
]

const props = defineProps<{
  origin?: { latitude: number; longitude: number; name?: string; free_spots?: number; status?: string } | null
  destination?: { latitude: number; longitude: number; name?: string; free_spots?: number; status?: string } | null
  useSockets?: boolean
  restStationsUrl?: string
  initialStations?: Array<{ idStation?: number; stationName?: string; latitude: number; length?: number; availableSlots?: number; type?: string }>
}>()

// REST base URL (Swagger UI mostró puerto 8005). Can be overridden via prop `restStationsUrl`.
// El endpoint en `estaciones-service` expone `GET /stations` (controller), así que apuntamos ahí.
const restUrl = props.restStationsUrl ?? 'http://localhost:8005/stations'

// No longer emit station-click from the map; clicks only show info in the overlay

const { t: $t } = useI18n()
const map = ref<LeafletMap | null>(null)
const isMounted = ref<boolean>(false)
const bicycleFactory = new BicycleFactory()
const wsService = new BicycleWebSocketService(bicycleFactory)
const stationFactory = new StationFactory()
const stationWsService = new StationWebSocketService(stationFactory)
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

const originPlain = computed(() => (props.origin ? JSON.parse(JSON.stringify(props.origin)) : null))
const destinationPlain = computed(() => (props.destination ? JSON.parse(JSON.stringify(props.destination)) : null))

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

function addOriginMarker(o: { latitude: number; longitude: number; name?: string }) {
  if (!map.value || !isMounted.value) return
  if (originMarker) {
    originMarker.remove()
    originMarker = null
  }
  console.log('🔵 Agregando marcador ORIGEN en:', { lat: o.latitude, lng: o.longitude, name: o.name })
  console.log('🔵 Verificar estaciones mock:', mockStations.map(s => ({ name: s.nameStation, lat: s.latitude, lng: s.longitude })))

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
  if (o.name) {
    const popupText = `<strong>${$t('reservation.map.markerOrigin')}</strong><br/>${o.name}<br/>${$t('reservation.map.markerCoords', { lat: o.latitude.toFixed(4), lng: o.longitude.toFixed(4) })}`
    originMarker.bindPopup(popupText)
  }
  setMountedTimeout(() => originMarker?.openPopup(), 200)
}

function addDestMarker(d: { latitude: number; longitude: number; name?: string }) {
  if (!map.value || !isMounted.value) return
  if (destMarker) {
    destMarker.remove()
    destMarker = null
  }
  console.log('🔵 Agregando marcador DESTINO en:', { lat: d.latitude, lng: d.longitude, name: d.name })

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
  if (d.name) {
    const popupText = `<strong>${$t('reservation.map.markerDestination')}</strong><br/>${d.name}<br/>${$t('reservation.map.markerCoords', { lat: d.latitude.toFixed(4), lng: d.longitude.toFixed(4) })}`
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

// Fetch stations from REST endpoint and render them. Returns true if successful.
async function fetchStationsFromRest(): Promise<boolean> {
  const candidates = [restUrl]
  // try common suffix
  if (!restUrl.endsWith('/list')) candidates.push(restUrl.replace(/\/$/, '') + '/list')
  if (!restUrl.endsWith('/all')) candidates.push(restUrl.replace(/\/$/, '') + '/all')

  for (const url of candidates) {
    try {
      console.log('[Map] trying REST stations URL:', url)
      const res = await fetch(url)
      if (!res.ok) {
        console.warn('[Map] REST stations fetch failed, status', res.status, 'for', url)
        continue
      }
      const data = await res.json()

      // try common shapes (array or wrapped)
      let list: any[] = []
      if (Array.isArray(data)) list = data
      else if (Array.isArray(data.content)) list = data.content
      else if (Array.isArray(data._embedded?.estaciones)) list = data._embedded.estaciones
      else {
        for (const v of Object.values(data)) if (Array.isArray(v)) { list = v as any[]; break }
      }

      if (!list.length) {
        console.warn('[Map] REST returned no station list for', url)
        continue
      }

      // render markers for each station
      list.forEach(s => {
        const lat = s.latitude ?? s.lat
        const lng = s.length ?? s.longitude ?? s.lng
        const name = s.stationName ?? s.nameStation ?? s.name
        const id = s.idStation ?? s.id
        if (typeof lat === 'number' && typeof lng === 'number') {
          const mk = L.marker([lat, lng]).addTo(map.value as LeafletMap)
          const popup = `<strong>${name ?? 'Estación'}</strong><br/>Tipo: ${s.type ?? '-'}<br/>CCTV: ${s.cctvStatus ?? '-'}<br/>ID: ${id}`
          mk.bindPopup(popup)
          // on hover show popup
          mk.on('mouseover', () => mk.openPopup())
          mk.on('mouseout', () => mk.closePopup())
        }
      })

      stationsRendered.value = true
      console.log('[Map] stations loaded from', url, 'count:', list.length)
      return true
    } catch (err) {
      console.warn('[Map] Error trying URL', url, err)
      continue
    }
  }

  console.warn('[Map] All REST station URL candidates failed')
  return false
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

  // Cargar datos locales de prueba inmediatamente (fallback)
  mockStations.forEach((st: StationLike) => {
    const marker = stationFactory.getStationMarker(st as unknown as StationLike)
    marker.setTranslator($t)
    marker.render(map.value as LeafletMap)
    try {
      marker.onClick((station: StationLike) => {
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
    } catch (e) { void e }
  })
  // If sockets disabled, try fetching stations from REST endpoint, else render local mocks and connect WS
  if (props.useSockets === false) {
    fetchStationsFromRest().then(ok => {
      if (!ok) {
        // fallback to provided initialStations prop, otherwise to local mocks
        const fallback = (props.initialStations && props.initialStations.length) ? props.initialStations : mockStations
        fallback.forEach(st => {
          const marker = stationFactory.getStationMarker(st as any)
          marker.render(map.value as LeafletMap)
          try {
            marker.onClick((station) => {
              const payload = {
                id: (station as any).idStation ?? (station as any).id,
                name: (station as any).nameStation ?? (station as any).name,
                latitude: station.latitude,
                longitude: station.longitude,
                free_spots: (station as any).availableSlots ?? (station as any).free_spots,
                type: (station as any).type ?? 'bike'
              }
              if (clickedStation.value && clickedStation.value.id === payload.id) clickedStation.value = null
              else clickedStation.value = payload
            })
          } catch (e) { void e }
        })
        stationsRendered.value = true
      }
    })
  } else {
    // Cargar datos locales de prueba inmediatamente (fallback)
    mockStations.forEach(st => {
      const marker = stationFactory.getStationMarker(st as any)
      marker.render(map.value as LeafletMap)
      try {
        marker.onClick((station) => {
          const payload = {
            id: (station as any).idStation ?? (station as any).id,
            name: (station as any).nameStation ?? (station as any).name,
            latitude: station.latitude,
            longitude: station.longitude,
            free_spots: (station as any).availableSlots ?? (station as any).free_spots,
            type: (station as any).type ?? 'bike'
          }
          if (clickedStation.value && clickedStation.value.id === payload.id) clickedStation.value = null
          else clickedStation.value = payload
        })
      } catch (e) { void e }
    })
    stationsRendered.value = true
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
