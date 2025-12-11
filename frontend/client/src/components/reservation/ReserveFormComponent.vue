<template>
  <div class="reservation-panel">
    <h1>{{ $t('reservation.form.stationDetails') }}</h1>

    <h3 v-if="props.station">{{ props.station.nameStation }}</h3>
    <h3 v-else>{{ $t('reservation.form.selectStation') || 'Selecciona una estación' }}</h3>

    <!-- Disponibilidad de bicicletas -->
    <div v-if="props.station" class="availability-container">
      <div class="availability-item mechanical">
        <div class="bike-icon">🚲</div>
        <div class="availability-info">
          <span class="availability-label">{{ $t('reservation.form.mechanical') }}</span>
          <span class="availability-count">{{ props.station.mechanical || props.station.availableMechanicBikes || 0 }}</span>
        </div>
      </div>
      <div class="availability-item electric">
        <div class="bike-icon">⚡</div>
        <div class="availability-info">
          <span class="availability-label">{{ $t('reservation.form.electric') }}</span>
          <span class="availability-count">{{ props.station.electric || props.station.availableElectricBikes || 0 }}</span>
        </div>
      </div>
    </div>

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
      <h3 class="balance-title">{{ $t('reservation.form.balance') }}</h3>
      <div class="balance-content">
        <strong class="balance-value">{{ formattedBalance }}</strong>
        <select v-model="selectedCurrency" class="currency-select">
          <option value="USD">USD</option>
          <option value="COP">COP</option>
          <option value="EUR">EUR</option>
        </select>
        <button class="btn-secondary" @click="recharge">{{ $t('reservation.form.recharge') }}</button>
      </div>
    </div>

    <p class="warning">
      ⚠️ {{ $t('reservation.form.warning') }} <strong>{{ $t('reservation.form.warningMinutes') }}</strong>.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, ref } from 'vue'
import UltimaMilla from "@/components/reservation/UltimaMilla.vue"
import { getAuth } from 'firebase/auth'
import { useTravelStore } from '@/stores/travel'
import { useI18n } from 'vue-i18n'
import { useReservation } from '@/composables/useReservation'
import { useRouter } from 'vue-router'
import { fetchExchangeRate } from '@/services/currencyExchange'
import type { Station } from '@/models/Station'
import usePaymentStore from '@/stores/payment'

type StationLike = Partial<Station>

const router = useRouter()
const { t: $t } = useI18n()
const { setReservation } = useReservation()

// Props with default values (includes optional origin/destination to sync selection from map)
const props = withDefaults(defineProps<{
  station?: StationLike | null
  balance?: number
  origin?: StationLike | null
  destination?: StationLike | null
}>(), {
  station: () => ({
    idStation: 0,
    nameStation: '',
    latitude: 0,
    longitude: 0,
    totalSlots: 0,
    availableSlots: 0,
    timestamp: new Date(),
    mechanical: 0,
    electric: 0
  }),
  balance: 0,
  origin: null,
  destination: null
})

// emits tipados (incluye forwarding)
const emit = defineEmits<{
  reserve: [payload: { bikeType: string; rideType: string }]
  close: []
  "update:origin": [StationLike | null]
  "update:destination": [StationLike | null]
}>()

const bikeType = ref<string>('')
const rideType = ref<string>('')

const travelStore = useTravelStore()
const paymentStore = usePaymentStore()
const statusMessage = ref<string>('')

// Selector de moneda y tasas de conversión
const selectedCurrency = ref<'USD' | 'COP' | 'EUR'>('USD')
const exchangeRates = ref({ COP: 4000, EUR: 0.9 })

// Formato de balance con conversión de moneda
const formattedBalance = computed(() => {
  if (props.balance === null || props.balance === undefined) return '--'

  const amountInUSD = props.balance
  let displayAmount = amountInUSD

  if (selectedCurrency.value === 'COP') {
    displayAmount = amountInUSD * exchangeRates.value.COP
  } else if (selectedCurrency.value === 'EUR') {
    displayAmount = amountInUSD * exchangeRates.value.EUR
  }

  const locale = selectedCurrency.value === 'COP' ? 'es-CO' : selectedCurrency.value === 'EUR' ? 'de-DE' : 'en-US'

  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: selectedCurrency.value,
    minimumFractionDigits: selectedCurrency.value === 'COP' ? 0 : 2
  }).format(displayAmount)
})

// Actualizar tasa de cambio dinámicamente
const updateExchangeRate = async () => {
  try {
    if (selectedCurrency.value === 'COP') {
      const rateCOP = await fetchExchangeRate('USD', 'COP', 1)
      exchangeRates.value.COP = rateCOP
      console.log(`[ReserveForm] Tasa de cambio actualizada: 1 USD = ${rateCOP} COP`)
    } else if (selectedCurrency.value === 'EUR') {
      const rateEUR = await fetchExchangeRate('USD', 'EUR', 1)
      exchangeRates.value.EUR = rateEUR
      console.log(`[ReserveForm] Tasa de cambio actualizada: 1 USD = ${rateEUR} EUR`)
    }
  } catch (error) {
    console.error('[ReserveForm] Error obteniendo tasa de cambio:', error)
  }
}

watch(selectedCurrency, () => {
  updateExchangeRate()
})

const recharge = () => {
  router.push({ name: 'profile' })
}

function onOriginUpdate(origin: StationLike | null) {
  emit("update:origin", origin ?? null)
}

function onDestinationUpdate(destination: StationLike | null) {
  emit("update:destination", destination ?? null)
}

async function onConfirmRoute(payload: { origin: StationLike; destination: StationLike; bikeType: string; rideType: string }) {
  if (!payload) {
    window.alert($t('reservation.form.selectionAlert') || 'Por favor complete todos los campos')
    return
  }

  if (!payload.bikeType || !payload.rideType) {
    window.alert($t('reservation.form.selectionAlert') || 'Por favor seleccione tipo de bicicleta y tipo de viaje')
    return
  }

  if (!payload.origin || !payload.destination) {
    window.alert('Por favor seleccione estación de origen y destino')
    return
  }

  const availableBikes = payload.bikeType === 'electric'
    ? (payload.origin?.electric ?? payload.origin?.availableElectricBikes ?? 0)
    : (payload.origin?.mechanical ?? payload.origin?.availableMechanicBikes ?? 0)

  if (availableBikes <= 0) {
    const bikeTypeLabel = payload.bikeType === 'electric' ? 'eléctricas' : 'mecánicas'
    window.alert(`No hay bicicletas ${bikeTypeLabel} disponibles en la estación seleccionada`)
    return
  }

  const firebaseAuth = getAuth()
  const currentUser = firebaseAuth.currentUser
  const userUid = currentUser?.uid ?? null

  if (!userUid) {
    window.alert($t('reservation.form.mustLogin'))
    return
  }

  let currentBalance = props.balance ?? 0
  try {
    const [latestBalance] = await Promise.all([
      paymentStore.fetchBalance(userUid),
      paymentStore.fetchFines(userUid)
    ])
    currentBalance = latestBalance ?? 0
  } catch (error) {
    console.error('[ReserveFormComponent] Error verificando saldo o multas:', error)
  }

  const hasPendingFines = paymentStore.fines.some(fine => (fine.t_state ?? fine.state) !== 'PAID')
  if (hasPendingFines) {
    window.alert('Tienes multas pendientes por pagar. Ve a tu perfil para pagarlas antes de reservar.')
    return
  }

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
    })

    const resp = await travelStore.startTravel(userUid, stationStartId, stationEndId, bikeTypeMapped)
    console.log('[ReserveFormComponent] onConfirmRoute - Viaje iniciado (Redis):', resp)
    const slotMsg = resp?.slotId ? `Tu slot asignado es ${resp.slotId}` : ''
    const reservationMsg = resp?.reservationId ? `ID de reserva: ${resp.reservationId}` : ''
    statusMessage.value = [slotMsg, reservationMsg].filter(Boolean).join(' · ')

    const reservationPayload = {
      bikeType: payload.bikeType,
      rideType: payload.rideType,
      station: payload.origin,
      destination: payload.destination ?? props.destination,
      startResponse: resp
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

.status-message {
  margin: .75rem 0;
  padding: .6rem .8rem;
  background: #F1F8E9;
  color: #2E7D32;
  border: 1px solid #C5E1A5;
  border-radius: 8px;
}

[data-theme="dark"] .status-message {
  background: rgba(46, 125, 50, 0.15);
  color: var(--color-primary-light);
  border-color: rgba(46, 125, 50, 0.3);
}

.balance-container {
  margin: 1.5rem 0;
  padding: 1.25rem;
  background-color: var(--color-background-light);
  border-radius: var(--border-radius);
  border: 1px solid var(--color-border-light);
}

.balance-title {
  margin: 0 0 2.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-primary-light);
  display: block;
  width: 100%;
}

.balance-content {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 1rem;
  width: 100%;
}

.balance-value {
  font-size: 1.2rem;
  color: var(--color-primary-light);
  font-weight: 700;
  margin: 0;
  padding: 0;
}

.currency-select {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border-light);
  border-radius: var(--border-radius);
  background-color: var(--color-background-light);
  color: var(--color-text-primary-light);
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  min-width: 75px;
  text-align: center;
  transition: border-color 0.3s ease;

  &:focus {
    outline: none;
    border-color: var(--color-primary-light);
    box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.2);
  }

  &:hover {
    border-color: var(--color-primary-light);
  }
}

[data-theme="dark"] .balance-container {
  background-color: var(--color-background-dark);
  border-color: var(--color-border-dark);
}

[data-theme="dark"] .balance-title {
  color: var(--color-text-primary-dark);
}

[data-theme="dark"] .balance-value {
  color: var(--color-primary-light);
}

[data-theme="dark"] .currency-select {
  background-color: var(--color-surface-dark);
  color: var(--color-text-primary-dark);
  border-color: var(--color-border-dark);
}

.availability-container {
  display: flex;
  gap: 0.6rem;
  margin: 0.6rem 0 0.9rem;
  padding: 0.6rem;
  background: var(--color-background-light);
  border: 1px solid var(--color-border-light);
  border-radius: 6px;
}

.availability-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.6rem;
  border-radius: 5px;
  transition: all 0.2s ease;
  
  &.mechanical {
    background: rgba(25, 118, 210, 0.08);
    border: 1px solid rgba(25, 118, 210, 0.2);
    
    .bike-icon {
      background: linear-gradient(135deg, #1976D2, #1565C0);
    }
    
    &:hover {
      background: rgba(25, 118, 210, 0.12);
    }
  }
  
  &.electric {
    background: rgba(255, 160, 0, 0.08);
    border: 1px solid rgba(255, 160, 0, 0.2);
    
    .bike-icon {
      background: linear-gradient(135deg, #FFA000, #F57C00);
    }
    
    &:hover {
      background: rgba(255, 160, 0, 0.12);
    }
  }
}

.bike-icon {
  font-size: 1.2rem;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  flex-shrink: 0;
  color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.availability-info {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  flex: 1;
}

.availability-label {
  font-size: 0.7rem;
  color: var(--color-text-secondary-light);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.availability-count {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-text-primary-light);
}

[data-theme="dark"] .availability-container {
  background: var(--color-surface-dark);
  border-color: var(--color-border-dark);
}

[data-theme="dark"] .availability-item {
  &.mechanical {
    background: rgba(25, 118, 210, 0.15);
    border-color: rgba(25, 118, 210, 0.3);
    
    &:hover {
      background: rgba(25, 118, 210, 0.2);
    }
  }
  
  &.electric {
    background: rgba(255, 160, 0, 0.15);
    border-color: rgba(255, 160, 0, 0.3);
    
    &:hover {
      background: rgba(255, 160, 0, 0.2);
    }
  }
}

[data-theme="dark"] .availability-label {
  color: var(--color-text-secondary-dark);
}

[data-theme="dark"] .availability-count {
  color: var(--color-text-primary-dark);
}

@media (max-width: 768px) {
  .availability-container {
    flex-direction: column;
    gap: 0.5rem;
    padding: 0.5rem;
  }
  
  .availability-item {
    padding: 0.5rem;
  }
  
  .bike-icon {
    font-size: 1.1rem;
    width: 1.8rem;
    height: 1.8rem;
  }
  
  .availability-count {
    font-size: 1rem;
  }
  
  .availability-label {
    font-size: 0.65rem;
  }
}

[data-theme="dark"] .balance-value {
  color: var(--color-primary-light);
}

[data-theme="dark"] .currency-select {
  background-color: var(--color-background-dark);
  border-color: var(--color-border-dark);
  color: var(--color-text-primary-dark);

  &:hover {
    border-color: var(--color-primary-light);
  }
}
</style>
