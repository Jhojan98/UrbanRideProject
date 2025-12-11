// filepath: /home/xmara/UD/Design of Software/UrbanRideProject/frontend/client/src/components/reservation/UltimaMilla.vue
<template>
  <div class="ultima-milla">
    <h2>{{ rideType === 'short_trip' ? $t('reservation.ultimaMilla.title') : $t('reservation.ultimaMilla.longTripTitle') }}</h2>
    <p class="sub">{{ rideType === 'short_trip' ? $t('reservation.ultimaMilla.subtitle') : $t('reservation.ultimaMilla.longTripSubtitle') }}</p>

    <!-- ORIGEN -->
    <div class="select-box">
      <label>📍 {{ rideType === 'short_trip' ? $t('reservation.ultimaMilla.originLabel') : $t('reservation.ultimaMilla.originLongTripLabel') }}</label>
      <select v-model="selectedOrigin">
        <option :value="null" disabled>{{ $t('reservation.ultimaMilla.selectPoint') }}</option>
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
      <label>🚲 {{ $t('reservation.ultimaMilla.destinationLabel') }}</label>
      <select v-model="selectedDest">
        <option :value="null" disabled>{{ $t('reservation.ultimaMilla.selectStation') }}</option>
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

    <!-- Información de disponibilidad del destino -->
    <div v-if="selectedDest" class="availability-container">
      <div class="availability-item mechanical">
        <div class="bike-icon">🚲</div>
        <div class="availability-info">
          <span class="availability-label">{{ $t('reservation.form.mechanical') }}</span>
          <span class="availability-count">{{ selectedDest.mechanical ?? selectedDest.availableMechanicBikes ?? 0 }}</span>
        </div>
      </div>
      <div class="availability-item electric">
        <div class="bike-icon">⚡</div>
        <div class="availability-info">
          <span class="availability-label">{{ $t('reservation.form.electric') }}</span>
          <span class="availability-count">{{ selectedDest.electric ?? selectedDest.availableElectricBikes ?? 0 }}</span>
        </div>
      </div>
    </div>

    <!-- Mensajes de validación -->
    <div v-if="validationMessage" class="validation-message" :class="{ error: hasError }">
      {{ validationMessage }}
    </div>

    <button
      class="btn-confirm"
      :disabled="!canConfirm"
      @click="confirm"
    >
      {{ $t('reservation.form.reserveBike') }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from "vue"
import { useI18n } from 'vue-i18n'
import { useStationStore } from '@/stores/station'

type StationOption = {
  idStation?: number
  nameStation?: string
  latitude?: number
  longitude?: number
  availableSlots?: number
  totalSlots?: number
  type?: string
  mechanical?: number
  electric?: number
  availableMechanicBikes?: number
  availableElectricBikes?: number
}

// Props recibidos desde ReserveFormComponent
const props = defineProps<{
  currentStation?: { idStation?: number; nameStation?: string; type?: string } | null
  bikeType?: string
  rideType?: string
}>()

const { t: $t } = useI18n()

const stationStore = useStationStore()

// Mantener los IDs de las estaciones seleccionadas para evitar perder la selección en actualizaciones del WS
const selectedOriginId = ref<number | null>(null)
const selectedDestinationId = ref<number | null>(null)

// Crear un mapa para búsqueda rápida de estaciones por ID
const stationMapByType = computed(() => {
  const map = {
    metros: new Map<number, StationOption>(),
    bikeStations: new Map<number, StationOption>()
  }

  // Metros
  stationStore.allStations.forEach(s => {
    if (s.type?.toUpperCase() === 'METRO') {
      map.metros.set(s.idStation, {
        idStation: s.idStation,
        nameStation: s.nameStation,
        latitude: s.latitude,
        longitude: s.longitude,
        availableSlots: s.availableSlots,
        totalSlots: s.totalSlots,
        type: s.type,
        mechanical: s.mechanical ?? 0,
        electric: s.electric ?? 0
      })
    }
  })

  // Bike stations
  stationStore.allStations.forEach(s => {
    if (s.type?.toUpperCase() !== 'METRO') {
      map.bikeStations.set(s.idStation, {
        idStation: s.idStation,
        nameStation: s.nameStation,
        latitude: s.latitude,
        longitude: s.longitude,
        availableSlots: s.availableSlots,
        totalSlots: s.totalSlots,
        type: s.type?.toUpperCase() || 'BIKE',
        mechanical: s.mechanical ?? 0,
        electric: s.electric ?? 0
      })
    }
  })

  return map
})

// Obtener estaciones desde el store, filtradas por tipo
const metros = computed<StationOption[]>(() => {
  return Array.from(stationMapByType.value.metros.values())
})

// Estaciones de bicicleta (no METRO) para recorrido largo
const bikeStations = computed<StationOption[]>(() => {
  return Array.from(stationMapByType.value.bikeStations.values())
})

// Todas las estaciones normalizadas (no se usa actualmente, disponible para futuros usos)
const _allStationsNormalized = computed<StationOption[]>(() => {
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

// Computed que mantiene la selección cuando se actualiza el store
const selectedOrigin = computed({
  get: () => {
    if (!selectedOriginId.value) return null
    return stationMapByType.value.metros.get(selectedOriginId.value) || null
  },
  set: (value) => {
    selectedOriginId.value = value?.idStation ?? null
  }
})

const selectedDest = computed({
  get: () => {
    if (!selectedDestinationId.value) return null
    return stationMapByType.value.bikeStations.get(selectedDestinationId.value) || null
  },
  set: (value) => {
    selectedDestinationId.value = value?.idStation ?? null
  }
})

const validationMessage = ref<string>('')
const hasError = ref<boolean>(false)

// Use props values
const bikeType = computed(() => props.bikeType ?? '')
const rideType = computed(() => props.rideType ?? 'short_trip')

// Origin options based on trip type
const originOptions = computed(() => {
  if (rideType.value === 'short_trip') {
    // Última milla (first/last mile): METRO stations only
    return metros.value
  } else {
    // Recorrido largo (long trip): BIKE stations only (no METRO)
    return bikeStations.value
  }
})

// Destination options based on trip type
const destinationOptions = computed(() => {
  if (rideType.value === 'short_trip') {
    // Última milla: destino debe ser estación de bici (no METRO)
    return bikeStations.value
  } else {
    // Recorrido largo: destino debe ser estación de bici (no METRO)
    return bikeStations.value
  }
})

// Habilitar el botón solo si todo está completo (disponibilidad se valida al confirmar)
const hasSelectedOrigin = computed(() => !!selectedOrigin.value)
const hasSelectedDestination = computed(() => !!selectedDest.value)
const hasSelectedBikeAndRide = computed(() => !!bikeType.value && !!rideType.value)

const canConfirm = computed(() =>
  hasSelectedOrigin.value &&
  hasSelectedDestination.value &&
  hasSelectedBikeAndRide.value
)

const emit = defineEmits<{
  confirm: [{ origin: StationOption; destination: StationOption; bikeType: string; rideType: string }]
  "update:origin": [StationOption | null]
  "update:destination": [StationOption | null]
}>()

// Disable origin stations based on bike type availability
function isOriginDisabled(station: StationOption) {
  // Si no hay tipo de bici seleccionado, no deshabilitar ninguna por disponibilidad
  if (!bikeType.value) return false

  // METRO stations are never disabled (don't require available bikes)
  if (station.type?.toUpperCase() === 'METRO') return false

  // Residential stations can be origin (don't need bikes to start)
  if (station.type?.toUpperCase() === 'RESIDENCIAL' || station.type?.toUpperCase() === 'RESIDENTIAL') return false

  // Para estaciones de bici, verificar disponibilidad del tipo seleccionado
  const mechanical = station.mechanical ?? station.availableMechanicBikes ?? 0
  const electric = station.electric ?? station.availableElectricBikes ?? 0
  const available = bikeType.value === 'mechanical' ? mechanical : electric

  return available === 0
}

// Disable destination stations based on availability and if it's the same as origin
function isDestinationDisabled(station: StationOption) {
  // Get ID of current start station (already selected from ReserveFormComponent)
  const currentStationId = props.currentStation?.idStation ?? null

  // ===== MAIN RESTRICTION: Do not allow selecting the same station as origin as destination =====
  if (currentStationId !== null && station.idStation === currentStationId) {
    console.log(`[UltimaMilla] Destination disabled: ${station.nameStation} is the same as origin`)
    return true
  }

  // If no bike type selected, don't disable any by availability
  if (!bikeType.value) return false

  // METRO stations are never disabled as destination (can be endpoint)
  if (station.type?.toUpperCase() === 'METRO') {
    console.log(`[UltimaMilla] Destination allowed: ${station.nameStation} is METRO`)
    return false
  }

  // RESIDENTIAL stations can be destination even without bikes (valid endpoint)
  if (station.type?.toUpperCase() === 'RESIDENCIAL' || station.type?.toUpperCase() === 'RESIDENTIAL') {
    console.log(`[UltimaMilla] Destino permitido: ${station.nameStation} es RESIDENCIAL`)
    return false
  }

  // Para estaciones de bici, verificar disponibilidad del tipo seleccionado
  const mechanical = station.mechanical ?? station.availableMechanicBikes ?? 0
  const electric = station.electric ?? station.availableElectricBikes ?? 0
  const available = bikeType.value === 'mechanical' ? mechanical : electric

  const disabled = available === 0
  if (disabled) {
    console.log(`[UltimaMilla] Destino deshabilitado: ${station.nameStation} sin ${bikeType.value}s disponibles`)
  }
  return disabled
}

// Emit transformed objects cuando cambia selectedOrigin (mantiene la selección incluso con WS updates)
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
}, { deep: false })

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
}, { deep: false })

// Reset selections cuando cambia el tipo de bicicleta o tipo de viaje
watch(() => [props.bikeType, props.rideType], () => {
  selectedOriginId.value = null
  selectedDestinationId.value = null
})

function confirm() {
  if (!canConfirm.value || !selectedOrigin.value || !selectedDest.value) {
    window.alert($t('reservation.form.selectionAlert') || 'Por favor completa todos los campos')
    return
  }

  // Validar disponibilidad del tipo de bicicleta elegido en la estación de origen
  const mechanical = selectedOrigin.value.mechanical ?? selectedOrigin.value.availableMechanicBikes ?? 0
  const electric = selectedOrigin.value.electric ?? selectedOrigin.value.availableElectricBikes ?? 0
  const available = bikeType.value === 'electric' ? electric : mechanical

  if (available <= 0) {
    const bikeTypeLabel = bikeType.value === 'electric' ? $t('reservation.form.electric') : $t('reservation.form.mechanical')
    window.alert(`${$t('reservation.form.noBikesAvailable') || 'No hay bicicletas disponibles'} (${bikeTypeLabel})`)
    return
  }

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

/* Estilos para disponibilidad de bicicletas */
.availability-container {
  display: flex;
  gap: 1rem;
  margin: 1rem 0;
  padding: 1rem;
  background: #f5f5f5;
  border-radius: 8px;
}

.availability-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  background: white;
  flex: 1;
}

.availability-item.mechanical {
  border-left: 4px solid #2563eb;
}

.availability-item.electric {
  border-left: 4px solid #16a34a;
}

.bike-icon {
  font-size: 1.5rem;
}

.availability-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.availability-label {
  font-size: 0.875rem;
  color: #666;
  font-weight: 500;
}

.availability-count {
  font-size: 1.5rem;
  font-weight: bold;
  color: #2c3e50;
}

.validation-message {
  padding: 0.75rem 1rem;
  margin: 1rem 0;
  border-radius: 6px;
  background: #e3f2fd;
  color: #1565c0;
  font-size: 0.875rem;
}

.validation-message.error {
  background: #ffebee;
  color: #c62828;
}

.sub {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}
</style>
