<template>
  <div class="reservation-panel">
    <h1>{{ $t('reservation.form.stationDetails') }}</h1>

    <h3 v-if="props.station">{{ props.station.nameStation }}</h3>
    <h3 v-else>{{ $t('reservation.form.selectStation') || 'Selecciona una estación' }}</h3>

    <p v-if="props.station" class="availability">
      🚲 {{ $t('reservation.form.bikesAvailable') }}: <strong>{{ props.station.availableSlots }}</strong>
    </p>

    <p v-if="props.station?.totalSlots" class="availability">
      🅿️ {{ $t('reservation.form.totalSlots') }}: <strong>{{ props.station.totalSlots }}</strong>
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

    <div class="status-message" v-if="statusMessage">
      {{ statusMessage }}
    </div>

    <div class="balance-container">
      {{ $t('reservation.form.balance') }} <strong>${{ props.balance }}</strong>
      <button class="btn-secondary" @click="recharge">{{ $t('reservation.form.recharge') }}</button>
    </div>

    <p class="warning">
      ⚠️ {{ $t('reservation.form.warning') }} <strong>{{ $t('reservation.form.warningMinutes') }}</strong>.
    </p>

    <!-- Bike reservation button removed to avoid duplication with Confirm Route -->
  </div>
</template>

<script setup lang="ts">
import UltimaMilla from "@/components/reservation/UltimaMilla.vue"
import { ref, defineEmits, withDefaults, defineProps } from 'vue'
import { getAuth } from 'firebase/auth'
import { useTravelStore } from '@/stores/travel'
import { useI18n } from 'vue-i18n';
import { useReservation } from '@/composables/useReservation'
import { useRouter } from 'vue-router'

interface Station {
  idStation?: number
  nameStation: string
  availableSlots: number
  totalSlots?: number
  // opcionales que UltimaMilla puede enviar
  latitude?: number
  longitude?: number
  type?: string
  mechanical?: number
  electric?: number
  timestamp?: Date
}

interface Props {
  station?: Station
  balance?: number
}

const router = useRouter()
const { t: $t } = useI18n();
const { setReservation } = useReservation()

// Props with default values (includes optional origin/destination to sync selection from map)
interface PropsWithSelection extends Props {
  origin?: Station | null
  destination?: Station | null
}

const props = withDefaults(defineProps<PropsWithSelection>(), {
  station: () => ({ nameStation: '', availableSlots: 0, totalSlots: 0 }),
  balance: 0,
  origin: null,
  destination: null
})

// emits tipados (incluye forwarding)
const emit = defineEmits<{
  reserve: [payload: { bikeType: string; rideType: string }]
  close: []
  "update:origin": [Station | null]
  "update:destination": [Station | null]
}>()

const bikeType = ref<string>('')
const rideType = ref<string>('')

const travelStore = useTravelStore()
const statusMessage = ref<string>('')

// acciones
async function reserve() {
  if (!bikeType.value || !rideType.value) {
    window.alert($t('reservation.form.selectionAlert'))
    return
  }

  // Preparar payload para viaje-service via gateway
  const firebaseAuth = getAuth()
  const currentUser = firebaseAuth.currentUser

  const userUid = currentUser?.uid ?? null

  // Determinar station ids usando el origin seleccionado (que viene del dropdown de UltimaMilla)
  const stationStartId = props.origin?.idStation ?? null
  const stationEndId = props.destination?.idStation ?? null

  const bikeTypeMapped = bikeType.value === 'electric' ? 'ELECTRIC' : 'MECHANIC'

  // Si no tenemos suficientes datos para iniciar el viaje, guardar localmente y emitir como antes
  if (!userUid || !stationStartId || !stationEndId) {
    console.log('[ReserveFormComponent] Faltan datos para enviar al backend:', {
      userUid: !!userUid,
      stationStartId,
      stationEndId,
      bikeType: bikeType.value,
      rideType: rideType.value
    });

    const reservationPayload = {
      bikeType: bikeType.value,
      rideType: rideType.value,
      station: props.origin,
      destination: props.destination
    }
    setReservation(reservationPayload)
    emit('reserve', { bikeType: bikeType.value, rideType: rideType.value })
    return
  }

  // Delegar la llamada al backend al store (sin hacer fetch directo en el componente)
  try {
    console.log('[ReserveFormComponent] Iniciando viaje con datos:', {
      userUid,
      stationStartId,
      stationEndId,
      bikeTypeMapped
    });

    const data = await travelStore.initiateTravel(
      userUid,
      Number(stationStartId),
      Number(stationEndId),
      bikeTypeMapped
    )
    console.log('[ReserveFormComponent] Respuesta del backend:', data)

    // Guardar la reserva localmente y notificar al padre
    const reservationPayload = {
      bikeType: bikeType.value,
      rideType: rideType.value,
      station: props.origin,
      destination: props.destination,
      startResponse: data
    }
    setReservation(reservationPayload)
    emit('reserve', { bikeType: bikeType.value, rideType: rideType.value })
  } catch (err) {
    console.error('[ReserveFormComponent] Error iniciando viaje:', err)
    window.alert($t('reservation.form.reserveError'))
  }
}

const recharge = () => {
  router.push({ name: 'profile' })
}

// Handlers para reenviar eventos recibidos desde UltimaMilla hacia el padre
function onOriginUpdate(origin: Station | null) {
  emit("update:origin", origin ?? null)
}

function onDestinationUpdate(destination: Station | null) {
  emit("update:destination", destination ?? null)
}

// Handler when UltimaMilla emits confirm (Reserve Bike button)
async function onConfirmRoute(payload: { origin: Station; destination: Station; bikeType: string; rideType: string }) {
  // Ejecutar reserva para ambos tipos de viaje (short_trip y long_trip)
  if (!payload) return

  // Obtener uid del usuario (Firebase)
  const firebaseAuth = getAuth()
  const currentUser = firebaseAuth.currentUser
  const userUid = currentUser?.uid ?? null

  if (!userUid) {
    window.alert($t('reservation.form.mustLogin'))
    return
  }

  // Determinar station ids usando el payload del evento (origin/destination del componente UltimaMilla)
  const stationStartId = Number(payload.origin?.idStation ?? 0)
  const stationEndId = Number(payload.destination?.idStation ?? 0)

  if (!stationStartId || !stationEndId) {
    window.alert($t('reservation.form.selectionAlert'))
    return
  }

  const bikeTypeMapped = payload.bikeType === 'electric' ? 'ELECTRIC' : 'MECHANIC'

  try {
    console.log('[ReserveFormComponent] onConfirmRoute - Iniciando viaje:', {
      userUid,
      stationStartId,
      stationEndId,
      bikeTypeMapped,
      originData: payload.origin
    });

    // Publishes temporary reservation to Redis via travel-service
    const resp = await travelStore.startTravel(userUid, stationStartId, stationEndId, bikeTypeMapped)
    console.log('[ReserveFormComponent] onConfirmRoute - Viaje iniciado (Redis):', resp)
    const slotMsg = resp?.slotId ? `Tu slot asignado es ${resp.slotId}` : ''
    const reservationMsg = resp?.reservationId ? `ID de reserva: ${resp.reservationId}` : ''
    statusMessage.value = [slotMsg, reservationMsg].filter(Boolean).join(' · ')

    // Guardar reserva local y emitir evento reserve para que el padre muestre ConfirmationComponent
    const reservationPayload = {
      bikeType: payload.bikeType,
      rideType: payload.rideType,
      station: payload.origin,
      destination: payload.destination ?? props.destination,
      startResponse: resp // incluye reservationId y slotId para mostrar al usuario
    }
    setReservation(reservationPayload)
    emit('reserve', { bikeType: payload.bikeType, rideType: payload.rideType })
  } catch (err: unknown) {
    console.error('[ReserveFormComponent] onConfirmRoute - Error starting trip:', err)
    window.alert($t('reservation.form.reserveError'))
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/maps.scss";
.status-message { margin: .75rem 0; padding: .6rem .8rem; background: #F1F8E9; color: #2E7D32; border: 1px solid #C5E1A5; border-radius: 8px; }
</style>
