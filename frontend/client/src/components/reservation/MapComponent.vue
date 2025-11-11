<template>
  <div class="reservation-panel">
    <div class="header">
      <h2>Detalles de la Estación</h2>
      <button class="close-btn" @click="$emit('close')">✕</button>
    </div>

    <h3>{{ props.station.name }}</h3>

    <p class="availability">
      🚲 Bicicletas Disponibles: <strong>{{ props.station.available }}</strong>
    </p>

    <p class="status" :class="props.station.status.toLowerCase()">
      {{ props.station.status }}
    </p>

    <label class="label">Tipo de Bicicleta:</label>
    <div class="select-group">
      <label><input type="radio" v-model="bikeType" value="mechanical" /> Mecánica</label>
      <label><input type="radio" v-model="bikeType" value="electric" /> Eléctrica</label>
    </div>

    <label class="label">Tipo de Viaje:</label>
    <div class="select-group vertical">
      <label>
        <input type="radio" v-model="rideType" value="short_trip" />
        Última Milla — 45 min máx — $17.500 + $250/min adicional
      </label>

      <label>
        <input type="radio" v-model="rideType" value="long_trip" />
        Recorrido Largo — 75 min máx — $25.000 + $1.000/min adicional
      </label>
    </div>

    <div class="balance-container">
      Saldo: <strong>${{ props.balance }}</strong>
      <button class="reload-btn" @click="recharge">Recargar</button>
    </div>

    <p class="warning">
      ⚠️ La bicicleta se reservará por <strong>10 minutos</strong>.
    </p>

    <button class="reserve-btn" @click="reserve">
      Reservar Bicicleta
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, defineEmits, withDefaults, defineProps } from 'vue'
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

function reserve() {
  if (!bikeType.value || !rideType.value) {
    window.alert('Por favor selecciona el tipo de bicicleta y el tipo de viaje.')
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

<style lang="scss">
@import "@/styles/maps.scss";
</style>