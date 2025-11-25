<template>
  <div class="profile">
    <div class="profile-header">
      <h1>{{ $t('profile.header.welcome_user', { name: 'CLIENTE EJEMPLO' }) }}</h1>
    </div>
    
    <div class="profile-content">
      <!-- Sección de Historial de Viajes -->
      <section class="profile-section">
        <h2 class="section-title">{{ $t('profile.trips.title') }}</h2>
        <div class="table-container">
          <table class="trips-table">
            <thead>
              <tr>
                <th>{{ $t('profile.trips.route') }}</th>
                <th>{{ $t('profile.trips.date') }}</th>
                <th>{{ $t('profile.trips.duration') }}</th>
                <th>{{ $t('profile.trips.cost') }}</th>
                <th>{{ $t('profile.trips.status') }}</th>
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
        <button class="btn-view-all">{{ $t('profile.trips.viewAll') }}</button>
      </section>

      <!-- Sección de Puntos de Fidelización -->
      <section class="profile-section">
        <h2 class="section-title">{{ $t('profile.loyalty.title') }}</h2>
        <div class="loyalty-card">
          <div class="points-display">
            <span class="points-label">{{ $t('profile.loyalty.pointsLabel') }}</span>
            <span class="points-value">1250</span>
          </div>
          <p class="points-info">{{ $t('profile.loyalty.missingPoints', { points: 750 }) }}</p>
          <div class="rewards">
            <h4>{{ $t('profile.loyalty.rewardsTitle') }}</h4>
            <ul class="rewards-list">
              <li>{{ $t('profile.loyalty.reward1') }}</li>
              <li>{{ $t('profile.loyalty.reward2') }}</li>
              <li>{{ $t('profile.loyalty.reward3') }}</li>
            </ul>
          </div>
          <button class="btn-redeem">{{ $t('profile.loyalty.redeem') }}</button>
        </div>
      </section>

      <!-- Sección de Saldo y Tarjeta -->
      <section class="profile-section">
        <h2 class="section-title">{{ $t('profile.balance.title') }}</h2>
        <div class="balance-card">
          <div class="balance-display">
            <span class="balance-label">{{ $t('profile.balance.currentBalance') }}</span>
            <span class="balance-value">$12.000 COP</span>
          </div>
          <div class="card-info">
            <h4>{{ $t('profile.balance.registeredCard') }}</h4>
            <div class="card-details">
              <span class="card-type">Visa</span>
              <span class="card-number">**** **** **** 1234</span>
              <span class="card-expiry">{{ $t('profile.balance.expires') }} 12/28</span>
            </div>
          </div>
          <div class="payment-actions">
            <button class="btn-payment" @click="managePayMethod">{{ $t('profile.balance.managePayments') }}</button>
            <button class="btn-add-balance" @click="addBalance">{{ $t('profile.balance.addBalance') }}</button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
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
const { t: $t } = useI18n();
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