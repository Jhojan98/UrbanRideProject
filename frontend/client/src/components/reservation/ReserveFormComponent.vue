<template>
  <div class="reservation-panel">
    <h1>{{ $t('reservation.form.stationDetails') }}</h1>

    <h3>{{ props.station.name }}</h3>

    <p class="availability">
      🚲 {{ $t('reservation.form.bikesAvailable') }}: <strong>{{ props.station.available }}</strong>
    </p>

    <p class="status" :class="props.station.status.toLowerCase()">
      {{ props.station.status }}
    </p>

    <label class="label">{{ $t('reservation.form.bikeType') }}</label>
    <div class="select-group">
      <label><input type="radio" v-model="bikeType" value="mechanical" /> {{ $t('reservation.form.mechanical') }}</label>
      <label><input type="radio" v-model="bikeType" value="electric" /> {{ $t('reservation.form.electric') }}</label>
    </div>

    <label class="label">{{ $t('reservation.form.rideType') }}</label>
    <div class="ride-options">
      <label class="ride-card">
        <input type="radio" v-model="rideType" value="short_trip" />
        <div>
          <strong>{{ $t('reservation.form.lastMile') }}</strong>
          <p>{{ $t('reservation.form.lastMileMax') }}</p>
          <span class="price">$17.500 + $250/min</span>
        </div>
      </label>

      <label class="ride-card">
        <input type="radio" v-model="rideType" value="long_trip" />
        <div>
          <strong>{{ $t('reservation.form.longTrip') }}</strong>
          <p>{{ $t('reservation.form.longTripMax') }}</p>
          <span class="price">$25.000 + $1.000/min</span>
        </div>
      </label>
    </div>

    <div v-if="rideType === 'short_trip' || rideType === 'long_trip'">
      <!-- Pasar bikeType y rideType a UltimaMilla -->
      <UltimaMilla
        :currentStation="props.station"
        :bikeType="bikeType"
        :rideType="rideType"
        @confirm="onConfirmRoute"
        @update:origin="onOriginUpdate"
        @update:destination="onDestinationUpdate"
      />
    </div>

    <div class="balance-container">
      {{ $t('reservation.form.balance') }} <strong>${{ props.balance }}</strong>
      <button class="btn-secondary" @click="recharge">{{ $t('reservation.form.recharge') }}</button>
    </div>

    <p class="warning">
      ⚠️ {{ $t('reservation.form.warning') }} <strong>{{ $t('reservation.form.warningMinutes') }}</strong>.
    </p>

    <button class="butn-primary" @click="reserve">
      {{ $t('reservation.form.reserveBike') }}
    </button>
  </div>
</template>

<script setup lang="ts">
import UltimaMilla from "@/components/reservation/UltimaMilla.vue"
import { ref, defineEmits, withDefaults, defineProps } from 'vue'
import { getAuth } from 'firebase/auth'
import userAuth from '@/stores/auth'
import { useTravelStore } from '@/stores/travel'
import { useI18n } from 'vue-i18n';
import { useReservation } from '@/composables/useReservation'
import { useRouter } from 'vue-router'

interface Station {
  id?: number
  name: string
  available: number
  status: string
  // opcionales que UltimaMilla puede enviar
  latitude?: number
  longitude?: number
  free_spots?: number
}

interface Props {
  station?: Station
  balance?: number
}

const router = useRouter()
const { t: $t } = useI18n();
const { setReservation } = useReservation()

// props con valores por defecto (incluye origin/destination opcionales para sincronizar selección desde el mapa)
interface PropsWithSelection extends Props {
  origin?: any | null
  destination?: any | null
}

const props = withDefaults(defineProps<PropsWithSelection>(), {
  station: () => ({ name: 'Selecciona una estación', available: 0, status: 'N/A' }),
  balance: 0,
  origin: null,
  destination: null
})

// emits tipados (incluye forwarding)
const emit = defineEmits<{
  reserve: [payload: { bikeType: string; rideType: string }]
  close: []
  "update:origin": [any | null]
  "update:destination": [any | null]
}>()

const bikeType = ref<string>('')
const rideType = ref<string>('')

const travelStore = useTravelStore()

// acciones
function reserve() {
  if (!bikeType.value || !rideType.value) {
    window.alert($t('reservation.form.selectionAlert'))
    return
  }

  // Preparar payload para viaje-service via gateway
  const auth = userAuth()
  const firebaseAuth = getAuth()
  const currentUser = firebaseAuth.currentUser

  const userUid = currentUser?.uid ?? null

  // Determinar station ids (intentar varias propiedades posibles)
  const stationStartId = (props.origin && (props.origin.id ?? props.origin.idStation)) || (props.station && (props.station.id ?? props.station.idStation)) || null
  const stationEndId = (props.destination && (props.destination.id ?? props.destination.idStation)) || null

  const bikeTypeMapped = bikeType.value === 'electric' ? 'ELECTRIC' : 'MECHANIC'

  // Si no tenemos suficientes datos para iniciar el viaje, guardar localmente y emitir como antes
  if (!userUid || !stationStartId || !stationEndId) {
    const reservationPayload = {
      bikeType: bikeType.value,
      rideType: rideType.value,
      station: props.station
    }
    setReservation(reservationPayload)
    emit('reserve', { bikeType: bikeType.value, rideType: rideType.value })
    return
  }

  // Llamada al gateway -> viaje-service
  const uri = `${auth.baseURL.replace(/\/$/, '')}/travel/start`
  const body = {
    userUid: userUid,
    stationStartId: Number(stationStartId),
    stationEndId: Number(stationEndId),
    bikeType: bikeTypeMapped
  }

  // Ejecutar petición y manejar resultado
  fetch(uri, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(body)
  }).then(async (res) => {
    if (!res.ok) {
      const txt = await res.text().catch(() => '')
      window.alert($t('reservation.form.reserveError') + '\n' + (txt || res.statusText))
      return
    }
    const data = await res.json().catch(() => null)
    console.log('Start travel response:', data)

    // Guardar la reserva localmente y notificar al padre
    const reservationPayload = {
      bikeType: bikeType.value,
      rideType: rideType.value,
      station: props.station,
      startResponse: data
    }
    setReservation(reservationPayload)
    emit('reserve', { bikeType: bikeType.value, rideType: rideType.value })
  }).catch(err => {
    console.error('Error calling start travel:', err)
    window.alert($t('reservation.form.reserveError'))
  })
}

const recharge = () => {
  router.push({ name: 'profile' })
}

// Handlers para reenviar eventos recibidos desde UltimaMilla hacia el padre
function onStationSelected(station: Station) {
  emit("update:destination", station ?? null)
}

function onOriginUpdate(origin: any) {
  emit("update:origin", origin ?? null)
}

function onDestinationUpdate(destination: any) {
  emit("update:destination", destination ?? null)
}

// Handler cuando UltimaMilla emite confirm (botón Confirmar Ruta)
async function onConfirmRoute(payload: { origin: any; destination: any; bikeType: string; rideType: string }) {
  // Solo ejecutar SHORT_TRIP (última milla) según requisito
  if (!payload || payload.rideType !== 'short_trip') {
    // fallback: si no es short_trip guardar local y emitir reserve
    const reservationPayload = {
      bikeType: bikeType.value,
      rideType: rideType.value,
      station: props.station
    }
    setReservation(reservationPayload)
    emit('reserve', { bikeType: bikeType.value, rideType: rideType.value })
    return
  }

  // Obtener uid del usuario (Firebase)
  const firebaseAuth = getAuth()
  const currentUser = firebaseAuth.currentUser
  const userUid = currentUser?.uid ?? null

  if (!userUid) {
    window.alert('Debe iniciar sesión para iniciar el viaje')
    return
  }

  // Determinar station ids desde los objetos emitidos
  const stationStartId = Number(payload.origin?.id ?? payload.origin?.idStation ?? props.station?.id ?? props.station?.idStation ?? 0)
  const stationEndId = Number(payload.destination?.id ?? payload.destination?.idStation ?? 0)

  if (!stationStartId || !stationEndId) {
    window.alert('No se pudo determinar estaciones origen/destino')
    return
  }

  const bikeTypeMapped = payload.bikeType === 'electric' ? 'ELECTRIC' : 'MECHANIC'

  try {
    const resp = await travelStore.startTravel({ userUid, stationStartId, stationEndId, bikeType: bikeTypeMapped })
    console.log('Viaje iniciado:', resp)
    // Guardar reserva local y emitir evento reserve para que el padre muestre ConfirmationComponent
    const reservationPayload = {
      bikeType: bikeType.value,
      rideType: rideType.value,
      station: props.station,
      startResponse: resp
    }
    setReservation(reservationPayload)
    emit('reserve', { bikeType: bikeType.value, rideType: rideType.value })
  } catch (err: any) {
    console.error('Error iniciando viaje:', err)
    window.alert('Error iniciando viaje: ' + (err?.message ?? String(err)))
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/maps.scss";
</style>