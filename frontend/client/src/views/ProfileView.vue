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
            <button class="btn-payment" @click="managePayMethod">Gestionar métodos de pago</button>
            <button class="btn-add-balance" @click="addBalance">Añadir saldo</button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
const router = useRouter();
interface Trip {
  id: number;
  route: string;
  date: string;
  duration: string;
  cost: string;
  status: string;
}

const managePayMethod = () => {
  router.push({ name: 'payment-methods' });
};
const addBalance = () => {
  router.push({ name: 'add-balance' });
};
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

<style lang="scss" scoped>
@import "@/styles/profile.scss";
</style>