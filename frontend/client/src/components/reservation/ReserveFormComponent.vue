<template>
  <div class="reservation-panel">
    <h1>Detalles de la Estación</h1>


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
    <div class="ride-options">
      <label class="ride-card">
        <input type="radio" v-model="rideType" value="short_trip" />
        <div>
          <strong>Última Milla</strong>
          <p>Máx 45 min</p>
          <span class="price">$17.500 + $250/min</span>
        </div>
      </label>

      <label class="ride-card">
        <input type="radio" v-model="rideType" value="long_trip" />
        <div>
          <strong>Recorrido Largo</strong>
          <p>Máx 75 min</p>
          <span class="price">$25.000 + $1.000/min</span>
        </div>
      </label>
    </div>
    <div v-if="rideType === 'short_trip'">
      <UltimaMilla :currentStation="props.station" @select="onStationSelected" />
    </div>


    <div class="balance-container">
      Saldo: <strong>${{ props.balance }}</strong>
      <button class="btn-secondary" @click="recharge">Recargar</button>
    </div>

    <p class="warning">
      ⚠️ La bicicleta se reservará por <strong>10 minutos</strong>.
    </p>

    <button class="butn-primary" @click="reserve">
      Reservar Bicicleta
    </button>
  </div>
</template>

<script setup lang="ts">
import UltimaMilla from "@/components/reservation/UltimaMilla.vue"
import { ref, defineEmits, withDefaults, defineProps } from 'vue'
import { useReservation } from '@/composables/useReservation'
import { useRouter } from 'vue-router'
const router = useRouter()


function onStationSelected(station: Station) {
  console.log("Estación elegida por última milla:", station)
}
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

<style lang="scss" scoped>
@import "@/styles/maps.scss";
</style>