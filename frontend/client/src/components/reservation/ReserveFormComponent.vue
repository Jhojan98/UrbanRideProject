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
import { ref, defineEmits, withDefaults, defineProps } from 'vue'
import { useI18n } from 'vue-i18n';
import { useReservation } from '@/composables/useReservation'
import { useRouter } from 'vue-router'
const router = useRouter()

interface Station {
  name: string
  available: number
  status: string
}

interface Props {
  station?: Station
  balance?: number
}

const recharge = () => {
  router.push({ name: 'profile' })
}

// withDefaults + defineProps con factory function para objetos
const props = withDefaults(defineProps<Props>(), {
  station: () => ({ name: 'Selecciona una estación', available: 0, status: 'N/A' }),
  balance: 0
})

// defineEmits tipado
const emit = defineEmits<{
  reserve: [payload: { bikeType: string; rideType: string }]
  close: []
}>()

const { setReservation } = useReservation()

const bikeType = ref<string>('')
const rideType = ref<string>('')

const { t: $t } = useI18n();
function reserve() {
  if (!bikeType.value || !rideType.value) {
    window.alert($t('reservation.form.selectionAlert'))
    return
  }

  const reservationPayload = {
    bikeType: bikeType.value,
    rideType: rideType.value,
    station: props.station
  }

  // Guardar la reserva en el estado compartido
  setReservation(reservationPayload)

  // Emitir el evento para que el padre pueda reaccionar
  emit('reserve', {
    bikeType: bikeType.value,
    rideType: rideType.value
  })
}
</script>

<style lang="scss" scoped>
@import "@/styles/maps.scss";
</style>