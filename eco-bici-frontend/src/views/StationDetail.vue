<template>
  <div class="station-detail">
    <div class="station-card">
      <h1 class="station-name">Estación Centro</h1>
      <div class="station-info">
        <div class="availability">
          <span class="availability-text">Bicicletas disponibles: 7</span>
          <span class="availability-status">Alta Disponibilidad</span>
        </div>
        <!-- 
          @click es un evento de Vue que ejecuta el método goToReservation 
          cuando se hace clic en el botón
        -->
        <button class="btn-reserve" @click="goToReservation">Reservar</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StationDetail',
  methods: {
    // Método que se ejecuta al hacer clic en "Reservar"
    goToReservation() {
      // $router es una instancia de vue-router proporcionada por Vue
      // push navega a la nueva ruta
      this.$router.push('/reservation');
    }
  }
}
</script>

<style scoped>
.station-detail {
  min-height: calc(100vh - 140px);
  padding: 2rem;
  display: flex;
  justify-content: center;
  align-items: flex-start; /* Alinea al inicio en caso de contenido largo */
}

.station-card {
  background: #FFFFFF;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 500px;
}

.station-name {
  color: #004E61;
  text-align: center;
  margin-bottom: 2rem;
  font-size: 2rem;
}

.station-info {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  align-items: center;
}

.availability {
  text-align: center;
}

.availability-text {
  display: block; /* Hace que cada span ocupe su propia línea */
  font-size: 1.2rem;
  color: #004E61;
  margin-bottom: 0.5rem;
}

.availability-status {
  display: block;
  color: #2E7D32;
  font-weight: bold;
  font-size: 1.1rem;
}

.btn-reserve {
  background-color: #FBC02D;
  color: #004E61;
  border: none;
  padding: 12px 32px;
  border-radius: 6px;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-reserve:hover {
  background-color: #F9A825;
}
</style>