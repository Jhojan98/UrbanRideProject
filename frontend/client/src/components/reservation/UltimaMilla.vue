// filepath: /home/xmara/UD/DIseño de Software/UrbanRideProject/frontend/client/src/components/reservation/UltimaMilla.vue
<template>
  <div class="ultima-milla">
    <h2>{{ rideType === 'short_trip' ? 'Viaje de Última Milla' : 'Recorrido Largo' }}</h2>
    <p class="sub">{{ rideType === 'short_trip' ? 'Selecciona tu punto de partida (metro) y la estación de bicicletas destino' : 'Selecciona estación de origen y destino (bici a bici)' }}</p>

    <!-- ORIGEN -->
    <div class="select-box">
      <label>📍 {{ rideType === 'short_trip' ? 'Punto de Partida (Metro)' : 'Estación de Origen (Bici)' }}</label>
      <select v-model="selectedOrigin">
        <option :value="null" disabled>Seleccione un punto</option>
        <option
          v-for="m in (rideType === 'short_trip' ? metros : stationsFiltered)"
          :key="m.id"
          :value="m"
          :disabled="isStationDisabled(m)"
        >
          {{ m.name }}
        </option>
      </select>
    </div>

    <!-- DESTINO -->
    <div class="select-box">
      <label>🚲 Estación de Destino (Bici)</label>
      <select v-model="selectedDest">
        <option :value="null" disabled>Seleccione estación</option>
        <option
          v-for="s in stationsFiltered"
          :key="s.id"
          :value="s"
          :disabled="isStationDisabled(s)"
        >
          {{ s.name }}
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
import { ref, watch, defineEmits, computed, defineProps } from "vue"

// Props recibidos desde ReserveFormComponent
const props = defineProps<{
  bikeType?: string
  rideType?: string
}>()

// Datos de prueba con coordenadas en Villavicencio
const metros = [
  { id: 1, name: "Metro Estación Central", latitude: 4.1425, longitude: -73.6312, free_spots: 5, status: "ACTIVE", type: 'metro' },
  { id: 2, name: "Metro Sikuani", latitude: 4.1480, longitude: -73.6270, free_spots: 2, status: "ACTIVE", type: 'metro' }
]

const stations = [
  { id: 10, name: "Estación Centro", latitude: 4.1430, longitude: -73.6290, type: 'bike', mechanical: 3, electric: 2 },
  { id: 11, name: "Parque Sikuani", latitude: 4.1475, longitude: -73.6260, type: 'bike', mechanical: 1, electric: 1 },
  { id: 12, name: "Zona Universitaria", latitude: 4.1505, longitude: -73.6235, type: 'bike', mechanical: 0, electric: 0 }
]

const selectedOrigin = ref<any | null>(null)
const selectedDest = ref<any | null>(null)

// Use props values
const bikeType = computed(() => props.bikeType ?? '')
const rideType = computed(() => props.rideType ?? 'short_trip')

const emit = defineEmits<{
  confirm: [{ origin: any; destination: any; bikeType: string; rideType: string }]
  "update:origin": [any | null]
  "update:destination": [any | null]
}>()

// Filtrar estaciones según tipo de bicicleta seleccionada
const stationsFiltered = computed(() => {
  if (!bikeType.value) return stations
  return stations.map(s => ({
    ...s,
    availableForType: bikeType.value === 'mechanical' ? s.mechanical : s.electric
  }))
})

// Formato de disponibilidad según tipo de bici
function formatAvailability(station: any) {
  if (!bikeType.value) return '(Seleccione tipo de bici primero)'
  if (station.type === 'metro') return ''
  const available = bikeType.value === 'mechanical' ? station.mechanical : station.electric
  const label = bikeType.value === 'mechanical' ? '⚙️' : '⚡'
  return `${label} ${available} disponibles`
}

// Deshabilitar estaciones sin bicis del tipo seleccionado
function isStationDisabled(station: any) {
  if (!bikeType.value || station.type === 'metro') return false
  const mechanical = station.mechanical ?? 0
  const electric = station.electric ?? 0
  const available = bikeType.value === 'mechanical' ? mechanical : electric
  return available === 0
}

// Emit transformed objects when user selects from dropdown (no bidirectional sync to avoid loops)
watch(selectedOrigin, (o) => {
  if (o) {
    const mechanical = o.mechanical ?? 0
    const electric = o.electric ?? 0
    const origin = {
      latitude: o.latitude,
      longitude: o.longitude,
      name: o.name,
      free_spots: bikeType.value === 'mechanical' ? mechanical : electric,
      status: o.status ?? "ACTIVE"
    }
    emit("update:origin", origin)
  } else {
    emit("update:origin", null)
  }
})

watch(selectedDest, (d) => {
  if (d) {
    const mechanical = d.mechanical ?? 0
    const electric = d.electric ?? 0
    const destination = {
      latitude: d.latitude,
      longitude: d.longitude,
      name: d.name,
      free_spots: bikeType.value === 'mechanical' ? mechanical : electric
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