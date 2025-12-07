<template>
  <div class="profile-container">
    <!-- Sidebar -->
    <aside class="profile-sidebar">
      <nav class="sidebar-nav">
        <button
          @click="activeTab = 'overview'"
          :class="['sidebar-btn', { active: activeTab === 'overview' }]"
        >
          {{ $t('profile.tabs.overview') }}
        </button>
        <button
          @click="activeTab = 'trips'"
          :class="['sidebar-btn', { active: activeTab === 'trips' }]"
        >
          {{ $t('profile.tabs.trips') }}
        </button>
        <button
          @click="activeTab = 'fines'"
          :class="['sidebar-btn', { active: activeTab === 'fines' }]"
        >
          {{ $t('profile.tabs.fines') }}
        </button>
      </nav>
    </aside>

    <!-- Main Content -->
    <main class="profile-main">
      <!-- Overview Tab -->
      <div v-if="activeTab === 'overview'" class="profile">
        <div class="profile-header">
          <h1>{{ welcomeText }}</h1>
        </div>
        <div class="profile-content">
          <!-- Balance and Card Section (Centered) -->
          <section class="profile-section balance-section">
            <h2 class="section-title">{{ $t('profile.balance.title') }}</h2>
            <div class="balance-card centered-card">
              <div class="currency-selector">
                <label for="currency-select">{{ $t('profile.balance.currency') }}:</label>
                <select
                  id="currency-select"
                  v-model="selectedCurrency"
                  class="currency-select"
                >
                  <option value="USD">USD - {{ $t('balance.currencies.USD') }}</option>
                  <option value="COP">COP - {{ $t('balance.currencies.COP') }}</option>
                </select>
              </div>
              <div class="balance-display">
                <span class="balance-label">{{ $t('profile.balance.currentBalance') }}</span>
                <span class="balance-value">
                  {{ formattedBalance }}
                  <button @click="refreshBalance" class="refresh-btn" title="Actualizar saldo">
                    ⟳
                  </button>
                  <span v-if="isLoadingBalance" class="loading-spinner">↻</span>
                </span>
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
                <button class="btn-add-balance" @click="goToPaymentMethods">
                  {{ $t('profile.balance.addBalance') }}
                </button>
              </div>
            </div>
          </section>
        </div>
      </div>

      <!-- Trips Tab -->
      <div v-if="activeTab === 'trips'" class="profile">
        <div class="profile-header">
          <h1>{{ $t('profile.trips.title') }}</h1>
        </div>
        <div class="profile-content">
          <section class="profile-section">
            <div v-if="isLoadingTrips" class="loading-message">
              {{ $t('profile.loading') }}
            </div>
            <div v-else-if="travels.length === 0" class="empty-message">
              {{ $t('profile.trips.noTrips') }}
            </div>
            <div v-else class="table-container">
              <table class="trips-table">
                <thead>
                  <tr>
                    <th>{{ $t('profile.trips.startStation') }}</th>
                    <th>{{ $t('profile.trips.endStation') }}</th>
                    <th>{{ $t('profile.trips.date') }}</th>
                    <th>{{ $t('profile.trips.status') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="trip in travels" :key="trip.idTravel">
                    <td>{{ trip.fromIdStation }}</td>
                    <td>{{ trip.toIdStation || 'N/A' }}</td>
                    <td>{{ formatDate(trip.startedAt) }}</td>
                    <td>{{ trip.status }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </div>
      </div>

      <!-- Fines Tab -->
      <div v-if="activeTab === 'fines'" class="profile">
        <div class="profile-header">
          <h1>{{ $t('profile.fines.title') }}</h1>
        </div>
        <div class="profile-content">
          <section class="profile-section">
            <div v-if="isLoadingFines" class="loading-message">
              {{ $t('profile.loading') }}
            </div>
            <div v-else-if="fines.length === 0" class="empty-message">
              {{ $t('profile.fines.noFines') }}
            </div>
            <div v-else class="fines-list">
              <div v-for="fine in fines" :key="fine.k_user_fine" class="fine-card" :class="{ 'fine-paid': fine.t_state === 'PAID' }">
                <div class="fine-header">
                  <h3>{{ $t('profile.fines.fineId') }}: {{ fine.k_user_fine }}</h3>
                  <span class="fine-status" :class="{ paid: fine.t_state === 'PAID', pending: fine.t_state !== 'PAID' }">
                    {{ fine.t_state === 'PAID' ? $t('profile.fines.paid') : $t('profile.fines.pending') }}
                  </span>
                </div>
                <div class="fine-details">
                  <p><strong>{{ $t('profile.fines.reason') }}:</strong> {{ fine.n_reason }}</p>
                  <p><strong>{{ $t('profile.fines.amount') }}:</strong> {{ formatCost(fine.v_amount_snapshot) }}</p>
                  <p><strong>{{ $t('profile.fines.date') }}:</strong> {{ formatDate(fine.f_assigned_at) }}</p>
                  <p v-if="fine.fine?.d_description"><strong>{{ $t('profile.fines.description') }}:</strong> {{ fine.fine.d_description }}</p>
                </div>
                <div v-if="fine.t_state !== 'PAID'" class="fine-actions">
                  <button class="btn-pay-fine" @click="payFine(fine.k_user_fine, fine.v_amount_snapshot)">
                    {{ $t('profile.fines.payNow') }}
                  </button>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { getAuth, onAuthStateChanged, type User } from "firebase/auth";
import usePaymentStore from "@/stores/payment";
import { useTravelStore } from "@/stores/travel";
import { fetchExchangeRate } from "@/services/currencyExchange";
import type Travel from "@/models/Travel";
import type Fine from "@/models/Fine";

const router = useRouter();
const { t: $t } = useI18n();
const paymentStore = usePaymentStore();
const travelStore = useTravelStore();

// Datos del usuario
const uid = ref<string | null>(null);
const userName = ref<string | null>(null);
const balance = ref<number | null>(null);
const isLoadingBalance = ref(false);
const isLoadingTrips = ref(false);
const isLoadingFines = ref(false);

// Selector de moneda
const selectedCurrency = ref<'USD' | 'COP'>('USD');

// Dynamic exchange rates
const exchangeRates = ref<{ COP: number }>({
  COP: 4000 // Default value USD to COP
});

// Tab activo
const activeTab = ref<'overview' | 'trips' | 'fines'>('overview');

// Datos de viajes y multas
const travels = ref<Travel[]>([]);
const fines = ref<Fine[]>([]);

// Welcome text format
const welcomeText = computed(() => {
  const name = userName.value ?? "CLIENT";
  return `Welcome back, ${name}`;
});

// Format balance
const formattedBalance = computed(() => {
  if (balance.value === null) return "--";

  // Balance comes in dollars (not in cents), use directly
  const amountInUSD = balance.value;
  const displayAmount = selectedCurrency.value === 'COP'
    ? amountInUSD * exchangeRates.value.COP
    : amountInUSD;

  const locale = selectedCurrency.value === 'COP' ? 'es-CO' : 'en-US';

  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency: selectedCurrency.value,
    minimumFractionDigits: selectedCurrency.value === 'COP' ? 0 : 2
  }).format(displayAmount);
});

// Funciones helper para formateo
const formatDate = (date: string | number | Date | undefined): string => {
  if (!date) return 'N/A';
  const dateObj = typeof date === 'string' || typeof date === 'number' ? new Date(date) : date;
  return dateObj.toLocaleDateString('es-CO', { year: 'numeric', month: 'short', day: 'numeric' });
};

const formatCost = (cost: number | undefined): string => {
  if (cost === null || cost === undefined) return 'N/A';
  // Cost comes in dollars (not in cents), use directly
  const costInUSD = typeof cost === 'number' ? cost : 0;
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2
  }).format(costInUSD);
};

// Navegar a PaymentMethods
function goToPaymentMethods() {
  router.push({ name: "payment-methods" });
}

// Update exchange rate dynamically
const updateExchangeRate = async () => {
  if (selectedCurrency.value === 'COP') {
    try {
      const rateCOP = await fetchExchangeRate('USD', 'COP', 1);
      exchangeRates.value.COP = rateCOP;
      console.log(`Exchange rate updated: 1 USD = ${rateCOP} COP`);
    } catch (error) {
      console.error('Error obteniendo tasa de cambio, usando valor por defecto:', error);
      // Mantener valor por defecto en caso de error
    }
  }
};

// Observar cambios en la moneda seleccionada
watch(selectedCurrency, () => {
  updateExchangeRate();
});

// Pagar multa
async function payFine(fineId: number, amount: number | undefined) {
  if (amount === undefined) {
    console.warn($t('profile.fines.noAmount'));
    alert($t('profile.fines.noAmountError'));
    return;
  }

  if (!uid.value) {
    console.warn('Cannot process payment without user UID');
    alert($t('profile.fines.notIdentified'));
    return;
  }

  // Verificar saldo suficiente
  if (balance.value !== null && balance.value < amount) {
    alert($t('profile.fines.insufficientBalance', { balance: formattedBalance.value, fine: formatCost(amount) }));
    return;
  }

  // Confirm payment with user
  const confirmed = confirm($t('profile.fines.confirmPayment', {
    id: fineId,
    amount: formatCost(amount),
    balance: formattedBalance.value,
    remaining: formatCost((balance.value || 0) - amount)
  }));
  if (!confirmed) return;

  try {
    console.log(`Paying fine ${fineId} of ${amount} USD`);

    // Call store method to pay the fine
    const success = await paymentStore.payFine(fineId, uid.value, amount);

    if (!success) {
      alert(paymentStore.error || $t('profile.fines.paymentError'));
      return;
    }

    // Reload fines after payment
    await paymentStore.fetchFines(uid.value);
    fines.value = paymentStore.fines;

    // Update balance
    await fetchBalance();

    alert($t('profile.fines.paymentSuccess'));
  } catch (error) {
    console.error('Error paying fine:', error);
    alert($t('profile.fines.paymentError'));
  }
}

// Obtener uid y nombre desde Firebase
function attachFirebaseAuthListener() {
  const auth = getAuth();
  onAuthStateChanged(auth, (user: User | null) => {
    if (user) {
      uid.value = user.uid;
      userName.value = user.displayName ?? (user.email ? user.email.split("@")[0] : `user_${user.uid.substring(0, 6)}`);

      try {
        localStorage.setItem("uid", uid.value);
        localStorage.setItem("userName", userName.value);
      } catch (e) { /* ignore storage errors */ }

      fetchBalance();
      loadTripsAndFines();
    } else {
      const storedUid = localStorage.getItem("uid");
      const storedName = localStorage.getItem("userName");
      if (storedUid) {
        uid.value = storedUid;
        userName.value = storedName ?? "CLIENTE";
        fetchBalance();
        loadTripsAndFines();
      } else {
        uid.value = null;
        userName.value = "CLIENTE";
        balance.value = null;
      }
    }
  });
}

// Obtener balance del usuario
async function fetchBalance() {
  if (!uid.value) return;

  isLoadingBalance.value = true;
  try {
    const result = await paymentStore.fetchBalance(uid.value);
    balance.value = result ?? 0;

    if (result !== null) {
      console.log("Balance obtenido:", balance.value);
      localStorage.setItem("userBalance", (balance.value ?? 0).toString());
    } else {
      const storedBalance = localStorage.getItem("userBalance");
      balance.value = storedBalance ? parseInt(storedBalance, 10) : 0;
    }
  } finally {
    isLoadingBalance.value = false;
  }
}

// Refrescar balance manualmente
async function refreshBalance() {
  await fetchBalance();
}

// Cargar viajes y multas
async function loadTripsAndFines() {
  if (!uid.value) return;

  // Cargar viajes
  isLoadingTrips.value = true;
  try {
    await travelStore.fetchTravels(uid.value);
    travels.value = travelStore.travels;
  } catch (error) {
    console.error('Error cargando viajes:', error);
    travels.value = [];
  } finally {
    isLoadingTrips.value = false;
  }

  // Cargar multas
  isLoadingFines.value = true;
  try {
    await paymentStore.fetchFines(uid.value);
    fines.value = paymentStore.fines;
  } catch (error) {
    console.error('Error cargando multas:', error);
    fines.value = [];
  } finally {
    isLoadingFines.value = false;
  }
}

// Configure listeners to update balance
function setupBalanceListeners() {
  const handleFocus = () => {
    console.log("Ventana enfocada, actualizando balance...");
    fetchBalance();
  };

  window.addEventListener('focus', handleFocus);

  const lastPaymentTime = localStorage.getItem('last_payment_time');
  if (lastPaymentTime) {
    const now = Date.now();
    const paymentTime = parseInt(lastPaymentTime, 10);
    if (now - paymentTime < 5 * 60 * 1000) {
      console.log("Pago reciente detectado, forzando actualización");
      fetchBalance();
      localStorage.removeItem('last_payment_time');
    }
  }

  const intervalId = setInterval(() => {
    if (uid.value && document.visibilityState === 'visible') {
      fetchBalance();
    }
  }, 30000);

  onUnmounted(() => {
    clearInterval(intervalId);
    window.removeEventListener('focus', handleFocus);
  });
}

onMounted(() => {
  attachFirebaseAuthListener();
  setupBalanceListeners();

  // Cargar tasa de cambio inicial
  try {
    updateExchangeRate().then(() => {
      console.log('Tasa de cambio cargada en montaje');
    });
  } catch (error) {
    console.error('Error cargando tasa inicial:', error);
  }

  setTimeout(() => {
    fetchBalance();
  }, 1000);
});
</script>

<style lang="scss" scoped>
@import "@/styles/profile.scss";

// Estilos adicionales para centrar la tarjeta de saldo
.balance-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  .centered-card {
    width: 100%;
    max-width: 500px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;

    .currency-selector {
      margin-bottom: 1rem;
      display: flex;
      align-items: center;
      gap: 0.5rem;

      label {
        font-weight: 500;
        color: #333;
        font-size: 14px;
      }

      .currency-btn {
        padding: 0.4rem 1rem;
        border: 2px solid #e0e0e0;
        background: white;
        border-radius: 6px;
        cursor: pointer;
        font-weight: 500;
        font-size: 14px;
        transition: all 0.3s;

        &:hover {
          border-color: #0074d4;
        }

        &.active {
          background: #0074d4;
          color: white;
          border-color: #0074d4;
        }
      }
    }

    .balance-display {
      margin-bottom: 1.5rem;
      position: relative;
    }

    .card-info {
      margin-bottom: 1.5rem;
      width: 100%;
    }

    .payment-actions {
      display: flex;
      gap: 1rem;
      justify-content: center;
      flex-wrap: wrap;
    }
  }
}

.btn-add-balance {
  background: #0074d4;
  color: white;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: background 0.3s;
}

.btn-add-balance:hover {
  background: #0056a3;
}

.refresh-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  margin-left: 8px;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.3s;
}

.refresh-btn:hover {
  color: #0074d4;
  background: #f0f0f0;
}

.loading-spinner {
  display: inline-block;
  animation: spin 1s linear infinite;
  margin-left: 8px;
  color: #666;
  font-size: 14px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// Responsive
@media (max-width: 768px) {
  .profile {
    padding: 1rem;
  }

  .balance-section .centered-card {
    max-width: 100%;
    padding: 0;
    border-radius: 0;
  }

  .currency-selector {
    flex-wrap: wrap;
    gap: 0.25rem !important;
    margin-bottom: 0.5rem !important;

    label {
      width: 100%;
      margin-bottom: 0.25rem;
      font-size: 12px;
    }

    .currency-btn {
      flex: 1;
      min-width: 50px;
      padding: 0.3rem 0.6rem !important;
      font-size: 12px !important;
    }
  }

  .balance-display {
    margin-bottom: 1rem !important;
  }

  .balance-value {
    font-size: 1.5rem !important;
  }

  .card-info {
    margin-bottom: 1rem !important;

    h4 {
      font-size: 0.9rem;
      margin-bottom: 0.5rem;
    }
  }

  .card-details {
    font-size: 0.85rem;
    gap: 0.25rem;
  }

  .payment-actions {
    width: 100%;

    .btn-add-balance {
      width: 100%;
      padding: 0.8rem !important;
      font-size: 14px;
    }
  }

  .btn-view-all {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .profile {
    min-height: calc(100vh - 120px);
    padding: 0.75rem;
  }

  .profile-header h1 {
    font-size: 1.5rem;
  }

  .section-title {
    font-size: 1.1rem;
  }

  .table-container {
    border-radius: 8px;
    overflow: hidden;
  }

  .trips-table th,
  .trips-table td {
    padding: 8px 4px;
    font-size: 0.75rem;
  }

  .currency-selector {
    label {
      font-size: 11px;
    }

    .currency-btn {
      padding: 0.25rem 0.4rem !important;
      font-size: 11px !important;
    }
  }

  .balance-value {
    font-size: 1.25rem !important;
  }

  .refresh-btn {
    font-size: 12px;
    margin-left: 4px;
    padding: 2px;
  }

  .card-type {
    font-size: 0.8rem;
  }

  .card-number,
  .card-expiry {
    font-size: 0.75rem;
  }
}

// Responsive
@media (max-width: 768px) {
  .balance-section .centered-card {
    max-width: 100%;
    padding: 0 10px;
  }
}
</style>
