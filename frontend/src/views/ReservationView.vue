<template>
  <div class="reservation">
    <div class="reservation-card">
      <h1 class="reservation-title">Tiempo restante de reserva</h1>
      <div class="timer">10:00</div>
      <div class="timer-label">minutos</div>
      
      <div class="trip-details">
        <h3>Detalles del viaje</h3>
        <div class="detail-item">
          <span class="detail-label">Tipo de viaje:</span>
          <span class="detail-value">Última Milla</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Costo estimado:</span>
          <span class="detail-value">$5.00</span>
        </div>
      </div>
      
      <button class="btn-confirm">Confirmar Reserva</button>
    </div>
  </div>
</template>

<script setup>

</script>

<style scoped>
.reservation {
  min-height: calc(100vh - 140px);
  padding: 2rem;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  background-color: #f8f9fa;
}

.reservation-card {
  background: #FFFFFF;
  padding: 3rem;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  text-align: center;
}

.reservation-title {
  color: #004E61;
  margin-bottom: 2rem;
  font-size: 1.5rem;
}

.timer {
  font-size: 4rem; /* Tiempo grande y visible */
  font-weight: bold;
  color: #2E7D32;
  margin-bottom: 0.5rem;
}

.timer-label {
  color: #666;
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.trip-details {
  background-color: #f8f9fa; /* Fondo gris claro para destacar */
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 2rem;
  text-align: left; /* Alinea el texto a la izquierda dentro de este contenedor */
}

.trip-details h3 {
  color: #004E61;
  margin-bottom: 1rem;
  text-align: center; /* Pero el título centrado */
}

.detail-item {
  display: flex;
  justify-content: space-between; /* Etiqueta a la izquierda, valor a la derecha */
  margin-bottom: 0.5rem;
}

.detail-label {
  color: #666;
  font-weight: 500;
}

.detail-value {
  color: #004E61;
  font-weight: bold;
}

.btn-confirm {
  background-color: #2E7D32;
  color: #FFFFFF;
  border: none;
  padding: 12px 32px;
  border-radius: 6px;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  width: 100%; /* Botón de ancho completo */
  transition: background-color 0.3s;
}

.btn-confirm:hover {
  background-color: #266629;
}
</style>