<template>
  <div class="profile">
    <div class="profile-header">
      <h1>Bienvenido de nuevo, CLIENTE EJEMPLO</h1>
    </div>
    
    <div class="profile-content">
      <!-- Sección de Historial de Viajes -->
      <section class="profile-section">
        <h2 class="section-title">Historial de Viajes</h2>
        <div class="table-container">
          <table class="trips-table">
            <thead>
              <tr>
                <th>Ruta</th>
                <th>Fecha</th>
                <th>Duración</th>
                <th>Costo</th>
                <th>Est</th>
              </tr>
            </thead>
            <tbody>

              <tr v-for="trip in trips" :key="trip.id">
                <td>{{ trip.route }}</td> 
                <td>{{ trip.date }}</td>
                <td>{{ trip.duration }}</td>
                <td>{{ trip.cost }}</td>
                <td>{{ trip.status }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <button class="btn-view-all">Ver todos los viajes</button>
      </section>

      <!-- Sección de Puntos de Fidelización -->
      <section class="profile-section">
        <h2 class="section-title">Puntos de Fidelización</h2>
        <div class="loyalty-card">
          <div class="points-display">
            <span class="points-label">Puntos actuales</span>
            <span class="points-value">1250</span>
          </div>
          <p class="points-info">Faltan 750 puntos para el siguiente nivel.</p>
          <div class="rewards">
            <h4>Tus recompensas</h4>
            <ul class="rewards-list">
              <li>1 hora de viaje gratis</li>
              <li>10% de descuento en suscripción</li>
              <li>Acceso a bicicletas premium</li>
            </ul>
          </div>
          <button class="btn-redeem">Canjear puntos</button>
        </div>
      </section>

      <!-- Sección de Saldo y Tarjeta -->
      <section class="profile-section">
        <h2 class="section-title">Saldo / Tarjeta Registrada</h2>
        <div class="balance-card">
          <div class="balance-display">
            <span class="balance-label">Saldo actual</span>
            <span class="balance-value">$12.000 COP</span>
          </div>
          <div class="card-info">
            <h4>Tarjeta Registrada</h4>
            <div class="card-details">
              <span class="card-type">Visa</span>
              <span class="card-number">**** **** **** 1234</span>
              <span class="card-expiry">Expira: 12/28</span>
            </div>
          </div>
          <div class="payment-actions">
            <button class="btn-payment">Gestionar métodos de pago</button>
            <button class="btn-add-balance">Añadir saldo</button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

interface Trip {
  id: number;
  route: string;
  date: string;
  duration: string;
  cost: string;
  status: string;
}

const trips = ref<Trip[]>([
  {
    id: 1,
    route: 'Parque Central a Calle Mayor',
    date: '2024-05-20',
    duration: '25 min',
    cost: '$15.000',
    status: 'C'
  },
  {
    id: 2,
    route: 'Plaza Nueva a Estación Tren',
    date: '2024-05-18',
    duration: '15 min',
    cost: '$14.000',
    status: 'C'
  },
  {
    id: 3,
    route: 'Avenida Sol a Mercado',
    date: '2024-05-15',
    duration: '30 min',
    cost: '$18.000',
    status: 'C'
  }
]);
</script>

<style scoped>
.profile {
  min-height: calc(100vh - 140px);
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto; /* Centra el perfil en pantallas grandes */
}

.profile-header {
  text-align: center;
  margin-bottom: 3rem;
}

.profile-header h1 {
  color: #004E61;
  font-size: 2rem;
}

.profile-content {
  display: grid;
  gap: 2rem; /* Espacio entre secciones */
}

.profile-section {
  background: #FFFFFF;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.section-title {
  color: #004E61;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.table-container {
  overflow-x: auto; /* Scroll horizontal en móviles si la tabla es ancha */
  margin-bottom: 1rem;
}

.trips-table {
  width: 100%;
  border-collapse: collapse; /* Elimina espacio entre bordes */
}

.trips-table th,
.trips-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0; /* Líneas separadoras */
}

.trips-table th {
  background-color: #f8f9fa; /* Fondo gris claro para encabezados */
  color: #004E61;
  font-weight: bold;
}

.trips-table tr:hover {
  background-color: #f8f9fa; /* Resalta fila al hover */
}

.btn-view-all {
  background-color: transparent;
  color: #2E7D32;
  border: 2px solid #2E7D32;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-view-all:hover {
  background-color: #2E7D32;
  color: #FFFFFF;
}

.loyalty-card,
.balance-card {
  background-color: #f8f9fa; /* Fondo gris claro para tarjetas internas */
  padding: 1.5rem;
  border-radius: 8px;
}

.points-display,
.balance-display {
  text-align: center;
  margin-bottom: 1rem;
}

.points-label,
.balance-label {
  display: block;
  color: #666;
  font-size: 0.9rem;
}

.points-value,
.balance-value {
  display: block;
  color: #2E7D32;
  font-size: 2rem;
  font-weight: bold;
  margin-top: 0.5rem;
}

.points-info {
  text-align: center;
  color: #666;
  margin-bottom: 1.5rem;
}

.rewards h4 {
  color: #004E61;
  margin-bottom: 1rem;
}

.rewards-list {
  list-style: none; /* Elimina viñetas por defecto */
  padding: 0;
  margin-bottom: 1.5rem;
}

.rewards-list li {
  padding: 0.5rem 0;
  color: #666;
  position: relative;
  padding-left: 1.5rem; /* Espacio para el check */
}

/* Crea checkmarks personalizados */
.rewards-list li:before {
  content: '✓';
  color: #2E7D32;
  position: absolute;
  left: 0;
}

.btn-redeem,
.btn-payment,
.btn-add-balance {
  width: 100%; /* Botones de ancho completo */
  padding: 12px;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-bottom: 0.5rem;
}

.btn-redeem {
  background-color: #FBC02D; /* Amarillo */
  color: #004E61;
}

.btn-redeem:hover {
  background-color: #F9A825;
}

.btn-payment {
  background-color: #004E61; /* Azul oscuro */
  color: #FFFFFF;
}

.btn-payment:hover {
  background-color: #003d4d;
}

.btn-add-balance {
  background-color: #2E7D32; /* Verde */
  color: #FFFFFF;
}

.btn-add-balance:hover {
  background-color: #266629;
}

.card-info h4 {
  color: #004E61;
  margin-bottom: 1rem;
}

.card-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.card-type {
  font-weight: bold;
  color: #004E61;
}

.card-number,
.card-expiry {
  color: #666;
}

/* Diseño responsive: en pantallas grandes usar grid de 2 columnas */
@media (min-width: 768px) {
  .profile-content {
    grid-template-columns: 2fr 1fr; /* Dos columnas: 2/3 y 1/3 */
  }
  
  /* La primera sección (historial) ocupa ambas columnas */
  .profile-section:first-child {
    grid-column: 1 / -1;
  }
}

/* Ajustes para móviles */
@media (max-width: 767px) {
  .profile {
    padding: 1rem; /* Menos padding en móviles */
  }
  
  .profile-section {
    padding: 1rem;
  }
}
</style>