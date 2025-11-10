<template>
  <div class="reservation-panel">
    <div class="header">
      <h2>Detalles de la Estación</h2>
      <button class="close-btn" @click="$emit('close')">✕</button>
    </div>

    <h3>{{ station.name }}</h3>

    <p class="availability">
      🚲 Bicicletas Disponibles: <strong>{{ station.available }}</strong>
    </p>

    <p class="status" :class="station.status.toLowerCase()">
      {{ station.status }}
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
      Saldo: <strong>${{ balance }}</strong>
      <button class="reload-btn">Recargar</button>
    </div>

    <p class="warning">
      ⚠️ La bicicleta se reservará por <strong>10 minutos</strong>.
    </p>

    <button class="reserve-btn" @click="reserve">
      Reservar Bicicleta
    </button>
  </div>
</template>

<script>
import { ref } from "vue";

export default {
  name: "ReservationPanel",

  // ⬇ Aquí declaramos las props de manera clásica
  props: {
  station: {
    type: Object,
    required: false,   // ✅ quitar lo obligatorio por ahora
    default: () => ({  // ✅ poner algo provisional para que no explote
        name: "Selecciona una estación",
        available: 0,
        status: "N/A"
        })
    },
    balance: {
        type: Number,
        required: false,
        default: 0
    }
    },

  setup(props, { emit }) {
    const bikeType = ref("");
    const rideType = ref("");

    function reserve() {
      if (!bikeType.value || !rideType.value) {
        alert("Por favor selecciona el tipo de bicicleta y el tipo de viaje.");
        return;
      }

      emit("reserve", {
        bikeType: bikeType.value,
        rideType: rideType.value
      });
    }

    return {
      bikeType,
      rideType,
      reserve
    };
  }
};
</script>

<style lang="scss">
@import "@/styles/maps.scss";
</style>