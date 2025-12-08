<template>
  <div class="profile-container">
    <main class="profile-main">
      <header class="profile-header">
        <h1>{{ $t('profile.tabs.overview') }}</h1>
      </header>

      <nav class="dashboard-nav">
        <button
          :class="['nav-btn', { active: activeDashboard === 'trips' }]"
          @click="activeDashboard = 'trips'"
        >
          {{ $t('profile.tabs.trips') }}
        </button>
        <button
          :class="['nav-btn', { active: activeDashboard === 'fines' }]"
          @click="activeDashboard = 'fines'"
        >
          {{ $t('profile.tabs.fines') }}
        </button>
      </nav>

      <section class="dashboard-content">
        <TripDashboard
          v-if="activeDashboard === 'trips'"
          :travels="travels"
          :loading="isLoadingTrips"
        />
        <FinesDashboard
          v-else
          :fines="fines"
          :loading="isLoadingFines"
          @pay="payFine"
        />
      </section>

      <BalanceCard
        class="dashboard-balance"
        :selected-currency="selectedCurrency"
        :formatted-balance="formattedBalance"
        :is-loading="isLoadingBalance"
        @refresh="refreshBalance"
        @add-balance="goToPaymentMethods"
        @currency-change="handleCurrencyChange"
      />
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
import TripDashboard from "@/components/dashboard/TripDashboard.vue";
import FinesDashboard from "@/components/dashboard/FinesDashboard.vue";
import BalanceCard from "@/components/dashboard/BalanceCard.vue";

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

// Dashboard tab activo (trips/fines)
const activeDashboard = ref<'trips' | 'fines'>('trips');

// Datos de viajes y multas
const travels = ref<Travel[]>([]);
const fines = ref<Fine[]>([]);

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

function handleCurrencyChange(currency: 'USD' | 'COP') {
  selectedCurrency.value = currency;
}

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
