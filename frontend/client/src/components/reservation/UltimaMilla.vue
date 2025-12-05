// filepath: /home/xmara/UD/DIseño de Software/UrbanRideProject/frontend/client/src/components/reservation/UltimaMilla.vue
<template>
  <div class="ultima-milla">
    <h2>{{ rideType === 'short_trip' ? 'Viaje de Última Milla' : 'Recorrido Largo' }}</h2>
    <p class="sub">{{ rideType === 'short_trip' ? 'Selecciona tu punto de partida (metro) y la estación de bicicletas destino' : 'Selecciona estación de origen y destino (bici a bici)' }}</p>

    <!-- ORIGEN -->
    <div class="select-box">
      <label>📍 {{ rideType === 'short_trip' ? 'Punto de Partida (Metro)' : 'Estación de Origen' }}</label>
      <select v-model="selectedOrigin">
        <option :value="null" disabled>Seleccione un punto</option>
        <option
          v-for="m in originOptions"
          :key="m.idStation"
          :value="m"
          :disabled="isOriginDisabled(m)"
        >
          {{ m.nameStation }} <span v-if="rideType === 'long_trip'">({{ m.type }})</span>
        </option>
      </select>
    </div>

    <!-- DESTINO -->
    <div class="select-box">
      <label>🚲 Estación de Destino</label>
      <select v-model="selectedDest">
        <option :value="null" disabled>Seleccione estación</option>
        <option
          v-for="s in destinationOptions"
          :key="s.idStation"
          :value="s"
          :disabled="isDestinationDisabled(s)"
        >
          {{ s.nameStation }} ({{ s.type }})
        </option>
      </select>
    </div>

    <button
      class="btn-confirm"
      :disabled="!selectedOrigin || !selectedDest || !bikeType"
      @click="confirm"
    >
      Confirmar Ruta
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, defineEmits, computed, defineProps, onMounted } from "vue"
import { useStationStore } from '@/stores/station'

// Props recibidos desde ReserveFormComponent
const props = defineProps<{
  currentStation?: { idStation?: number; nameStation?: string; type?: string } | null
  bikeType?: string
  rideType?: string
}>()

const stationStore = useStationStore()

// Obtener estaciones desde el store, filtradas por tipo
const metros = computed(() => {
  return stationStore.allStations.filter(s =>
    s.type?.toUpperCase() === 'METRO'
  ).map(s => ({
    idStation: s.idStation,
    nameStation: s.nameStation,
    latitude: s.latitude,
    longitude: s.longitude,
    availableSlots: s.availableSlots,
    totalSlots: s.totalSlots,
    type: s.type,
    mechanical: s.mechanical ?? 0,
    electric: s.electric ?? 0
  }))
})

// Todas las estaciones normalizadas (para viaje largo y destinos)
const allStationsNormalized = computed(() => {
  return stationStore.allStations.map(s => ({
    idStation: s.idStation,
    nameStation: s.nameStation,
    latitude: s.latitude,
    longitude: s.longitude,
    availableSlots: s.availableSlots,
    totalSlots: s.totalSlots,
    type: s.type?.toUpperCase() || 'BIKE',
    mechanical: s.mechanical ?? 0,
    electric: s.electric ?? 0
  }))
})

// Solo estaciones de bici (para compatibilidad con código anterior)
const stations = computed(() => {
  return allStationsNormalized.value.filter(s =>
    s.type === 'BIKE' || s.type === 'BICYCLE'
  )
})

const selectedOrigin = ref<any | null>(null)
const selectedDest = ref<any | null>(null)

// Use props values
const bikeType = computed(() => props.bikeType ?? '')
const rideType = computed(() => props.rideType ?? 'short_trip')

// Opciones de origen según tipo de viaje
const originOptions = computed(() => {
  if (rideType.value === 'short_trip') {
    // Última milla: solo estaciones METRO
    return metros.value
  } else {
    // Viaje largo: cualquier tipo de estación
    return allStationsNormalized.value
  }
})

// Opciones de destino: siempre cualquier tipo de estación
const destinationOptions = computed(() => {
  return allStationsNormalized.value
})

const emit = defineEmits<{
  confirm: [{ origin: any; destination: any; bikeType: string; rideType: string }]
  "update:origin": [any | null]
  "update:destination": [any | null]
}>()

// Deshabilitar estaciones de origen según disponibilidad del tipo de bici seleccionado
function isOriginDisabled(station: { idStation?: number; nameStation?: string; type?: string; mechanical?: number; electric?: number }) {
  // Si no hay tipo de bici seleccionado, no deshabilitar ninguna por disponibilidad
  if (!bikeType.value) return false

  // Las estaciones METRO nunca se deshabilitan (no requieren bicis disponibles)
  if (station.type?.toUpperCase() === 'METRO') return false

  // Estaciones residenciales pueden ser origen (no necesitan bicis para partir)
  if (station.type?.toUpperCase() === 'RESIDENCIAL' || station.type?.toUpperCase() === 'RESIDENTIAL') return false

  // Para estaciones de bici, verificar disponibilidad del tipo seleccionado
  const mechanical = station.mechanical ?? 0
  const electric = station.electric ?? 0
  const available = bikeType.value === 'mechanical' ? mechanical : electric

  return available === 0
}

// Deshabilitar estaciones de destino según disponibilidad y si es la misma que el origen
function isDestinationDisabled(station: { idStation?: number; nameStation?: string; type?: string; mechanical?: number; electric?: number }) {
  // Obtener el ID de la estación de partida actual (ya seleccionada desde ReserveFormComponent)
  const currentStationId = props.currentStation?.idStation ?? null

  // ===== RESTRICCIÓN PRINCIPAL: No permitir seleccionar como destino la misma estación que el origen =====
  if (currentStationId !== null && station.idStation === currentStationId) {
    console.log(`[UltimaMilla] Destino deshabilitado: ${station.nameStation} es la misma que el origen`)
    return true
  }

  // Si no hay tipo de bici seleccionado, no deshabilitar ninguna por disponibilidad
  if (!bikeType.value) return false

  // Las estaciones METRO nunca se deshabilitan como destino (pueden ser punto final)
  if (station.type?.toUpperCase() === 'METRO') {
    console.log(`[UltimaMilla] Destino permitido: ${station.nameStation} es METRO`)
    return false
  }

  // Las estaciones RESIDENCIALES pueden ser destino incluso sin bicis (punto final válido)
  if (station.type?.toUpperCase() === 'RESIDENCIAL' || station.type?.toUpperCase() === 'RESIDENTIAL') {
    console.log(`[UltimaMilla] Destino permitido: ${station.nameStation} es RESIDENCIAL`)
    return false
  }

  // Para estaciones de bici, verificar disponibilidad del tipo seleccionado
  const mechanical = station.mechanical ?? 0
  const electric = station.electric ?? 0
  const available = bikeType.value === 'mechanical' ? mechanical : electric

  const disabled = available === 0
  if (disabled) {
    console.log(`[UltimaMilla] Destino deshabilitado: ${station.nameStation} sin ${bikeType.value}s disponibles`)
  }
  return disabled
}

// Emit transformed objects when user selects from dropdown (no bidirectional sync to avoid loops)
watch(selectedOrigin, (o) => {
  if (o) {
    const origin = {
      idStation: o.idStation,
      nameStation: o.nameStation,
      latitude: o.latitude,
      longitude: o.longitude,
      availableSlots: o.availableSlots,
      totalSlots: o.totalSlots,
      type: o.type,
      mechanical: o.mechanical ?? 0,
      electric: o.electric ?? 0
    }
    emit("update:origin", origin)
  } else {
    emit("update:origin", null)
  }
})

watch(selectedDest, (d) => {
  if (d) {
    const destination = {
      idStation: d.idStation,
      nameStation: d.nameStation,
      latitude: d.latitude,
      longitude: d.longitude,
      availableSlots: d.availableSlots,
      totalSlots: d.totalSlots,
      type: d.type,
      mechanical: d.mechanical ?? 0,
      electric: d.electric ?? 0
    }
    emit("update:destination", destination)
  } else {
    emit("update:destination", null)
  }
})

// Reset selections when bike type or ride type changes
watch(() => [props.bikeType, props.rideType], () => {
  selectedOrigin.value = null
  selectedDest.value = null
})

function confirm() {
  emit("confirm", {
    origin: selectedOrigin.value,
    destination: selectedDest.value,
    bikeType: bikeType.value,
    rideType: rideType.value
  })
}

// Cargar estaciones al montar el componente
onMounted(async () => {
  if (stationStore.allStations.length === 0) {
    console.log('[UltimaMilla] Cargando estaciones desde el backend...')
    await stationStore.fetchStations()
    console.log('[UltimaMilla] Estaciones cargadas:', stationStore.allStations.length)
  } else {
    console.log('[UltimaMilla] Usando estaciones del store:', stationStore.allStations.length)
  }

  // Log detallado de tipos de estaciones
  console.log('[UltimaMilla] Tipos de estaciones disponibles:')
  stationStore.allStations.forEach(s => {
    console.log(`  - ${s.nameStation}: type="${s.type}" | mechanical=${s.mechanical} electric=${s.electric}`)
  })
})
</script>

<style scoped>
.ultima-milla { padding: 1.5rem; }
.select-box { margin-bottom: 1rem; display: flex; flex-direction: column; }
select { padding: .5rem; border-radius: 6px; border: 1px solid #ccc; }
.info-card { background: #fff; padding: 1rem; border-radius: 10px; margin-top: 1rem; box-shadow: 0 2px 4px rgba(0,0,0,.1); }
.status.active { color: green; font-weight: bold; }
.status.full { color: red; font-weight: bold; }
.btn-confirm { margin-top: 1rem; width: 100%; padding: .7rem; background: #2E7D32; color: white; border-radius: 8px; border: none; cursor: pointer; }
</style>
