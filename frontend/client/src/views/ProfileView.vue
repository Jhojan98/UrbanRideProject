<template>
  <div class="profile-container">
    <!-- Botón de menú para móvil -->
    <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen" v-if="windowWidth < 768">
      <i class="fa fa-bars"></i>
    </button>

    <!-- Sidebar -->
    <aside class="profile-sidebar" :class="{ 'mobile-open': mobileMenuOpen }">
      <nav class="sidebar-nav">
        <button
          @click="() => { activeTab = 'overview'; mobileMenuOpen = false; }"
          :class="['sidebar-btn', { active: activeTab === 'overview' }]"
        >
          {{ $t('profile.tabs.overview') }}
        </button>
        <button
          @click="() => { activeTab = 'trips'; mobileMenuOpen = false; }"
          :class="['sidebar-btn', { active: activeTab === 'trips' }]"
        >
          {{ $t('profile.tabs.trips') }}
        </button>
        <button
          @click="() => { activeTab = 'fines'; mobileMenuOpen = false; }"
          :class="['sidebar-btn', { active: activeTab === 'fines' }]"
        >
          {{ $t('profile.tabs.fines') }}
        </button>
        <button
          @click="() => { activeTab = 'complaints'; mobileMenuOpen = false; }"
          :class="['sidebar-btn', { active: activeTab === 'complaints' }]"
        >
          {{ $t('profile.tabs.complaints') }}
        </button>
        <button
          @click="() => { activeTab = 'reports'; mobileMenuOpen = false; }"
          :class="['sidebar-btn', { active: activeTab === 'reports' }]"
        >
          {{ $t('profile.tabs.reports') }}
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
        <div class="profile-content overview-content">
          <!-- Sección de Saldo y Tarjeta (Centrada) -->
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
                  <option value="USD">USD - Dólar</option>
                  <option value="COP">COP - Peso Colombiano</option>
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

      <!-- Complaints / Maintenance Tab -->
      <div v-if="activeTab === 'complaints'" class="profile">
        <div class="profile-header">
          <h1>{{ $t('profile.complaints.title') }}</h1>
          <p class="muted">{{ $t('profile.complaints.subtitle') }}</p>
        </div>
        <div class="profile-content cards-grid">
          <div class="action-card">
            <h3>{{ $t('profile.complaints.formTitle') }}</h3>
            <p>{{ $t('profile.complaints.formSubtitle') }}</p>
            <form class="complaint-form" @submit.prevent="submitComplaint">
              <label class="input-label">{{ $t('profile.complaints.descriptionLabel') }}</label>
              <textarea
                v-model="complaintForm.description"
                :placeholder="$t('profile.complaints.descriptionPlaceholder')"
                rows="3"
                required
              />

              <label class="input-label">{{ $t('profile.complaints.typeLabel') }}</label>
              <select v-model="complaintForm.type">
                <option value="BICYCLE">{{ $t('profile.complaints.typeOptions.bicycle') }}</option>
                <option value="SLOT">{{ $t('profile.complaints.typeOptions.slot') }}</option>
                <option value="STATION">{{ $t('profile.complaints.typeOptions.station') }}</option>
              </select>

              <label class="input-label">{{ $t('profile.complaints.travelIdLabel') }}</label>
              <input
                v-model="complaintForm.travelId"
                type="number"
                min="1"
                :placeholder="$t('profile.complaints.travelIdPlaceholder')"
              />

              <p v-if="complaintError" class="text-error">{{ complaintError }}</p>
              <p v-if="complaintSuccess" class="text-success">{{ complaintSuccess }}</p>

              <button class="btn-primary" type="submit" :disabled="supportStore.loading">
                {{ supportStore.loading ? $t('profile.complaints.submitting') : $t('profile.complaints.submit') }}
              </button>
            </form>
          </div>
          <div class="action-card">
            <h3>{{ $t('profile.complaints.maintenanceTitle') }}</h3>
            <p>{{ $t('profile.complaints.maintenanceSubtitle') }}</p>
            <button class="btn-primary" @click="openMaintenancePortal">{{ $t('profile.complaints.openMaintenance') }}</button>
          </div>
        </div>
      </div>

      <!-- Reports Tab -->
      <div v-if="activeTab === 'reports'" class="profile">
        <div class="profile-header">
          <h1>{{ $t('profile.reportsSection.title') }}</h1>
          <p class="muted">{{ $t('profile.reportsSection.subtitle') }}</p>
        </div>
        <div class="profile-content cards-grid">
          <div class="action-card">
            <h3>{{ $t('profile.reportsSection.cards.bicycleUsage') }}</h3>
            <div class="report-actions">
              <button class="btn-primary" @click="downloadReport('bicycle-usage.xlsx')">{{ $t('profile.reportsSection.downloadExcel') }}</button>
              <button class="btn-secondary" @click="downloadReport('bicycle-usage.pdf')">{{ $t('profile.reportsSection.downloadPdf') }}</button>
            </div>
          </div>
          <div class="action-card">
            <h3>{{ $t('profile.reportsSection.cards.stationDemand') }}</h3>
            <div class="report-actions">
              <button class="btn-primary" @click="downloadReport('station-demand.xlsx')">{{ $t('profile.reportsSection.downloadExcel') }}</button>
              <button class="btn-secondary" @click="downloadReport('station-demand.pdf')">{{ $t('profile.reportsSection.downloadPdf') }}</button>
            </div>
          </div>
          <div class="action-card">
            <h3>{{ $t('profile.reportsSection.cards.bicycleDemand') }}</h3>
            <div class="report-actions">
              <button class="btn-primary" @click="downloadReport('bicycle-demand.xlsx')">{{ $t('profile.reportsSection.downloadExcel') }}</button>
              <button class="btn-secondary" @click="downloadReport('bicycle-demand.pdf')">{{ $t('profile.reportsSection.downloadPdf') }}</button>
            </div>
          </div>
          <div class="action-card">
            <h3>{{ $t('profile.reportsSection.cards.dailyTrips') }}</h3>
            <div class="report-actions">
              <button class="btn-primary" @click="downloadReport('daily-trips.xlsx')">{{ $t('profile.reportsSection.downloadExcel') }}</button>
              <button class="btn-secondary" @click="downloadReport('daily-trips.pdf')">{{ $t('profile.reportsSection.downloadPdf') }}</button>
            </div>
          </div>
          <div class="action-card">
            <h3>{{ $t('profile.reportsSection.cards.maintenances') }}</h3>
            <div class="report-actions">
              <button class="btn-primary" @click="downloadReport('maintenances.xlsx')">{{ $t('profile.reportsSection.downloadExcel') }}</button>
              <button class="btn-secondary" @click="downloadReport('maintenances.pdf')">{{ $t('profile.reportsSection.downloadPdf') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, reactive } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { getAuth, onAuthStateChanged, type User } from "firebase/auth";
import usePaymentStore from "@/stores/payment";
import { useTravelStore } from "@/stores/travel";
import { useSupportStore } from "@/stores/support";
import { fetchExchangeRate } from "@/services/currencyExchange";
import type Travel from "@/models/Travel";
import type Fine from "@/models/Fine";

const router = useRouter();
const { t: $t } = useI18n();
const paymentStore = usePaymentStore();
const travelStore = useTravelStore();
const supportStore = useSupportStore();

// Mobile menu
const mobileMenuOpen = ref(false);
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 768);

// Datos del usuario
const uid = ref<string | null>(null);
const userName = ref<string | null>(null);
const balance = ref<number | null>(null);
const isLoadingBalance = ref(false);
const isLoadingTrips = ref(false);
const isLoadingFines = ref(false);

// Selector de moneda
const selectedCurrency = ref<'USD' | 'COP'>('USD');

// Tasas de conversión dinámicas
const exchangeRates = ref<{ COP: number }>({
  COP: 4000 // Valor por defecto USD a COP
});

// Tab activo
const activeTab = ref<'overview' | 'trips' | 'fines' | 'complaints' | 'reports'>('overview');

// Datos de viajes y multas
const travels = ref<Travel[]>([]);
const fines = ref<Fine[]>([]);

// Quejas
const complaintForm = reactive({
  description: "",
  type: "BICYCLE" as "BICYCLE" | "SLOT" | "STATION",
  travelId: "",
});
const complaintError = ref("");
const complaintSuccess = ref("");

// Texto de bienvenida
const welcomeText = computed(() => {
  const name = userName.value ?? "CLIENTE";
  return `Bienvenido de nuevo, ${name}`;
});

// Formato de balance
const formattedBalance = computed(() => {
  if (balance.value === null) return "--";

  // El balance viene en dólares (no en centavos), usar directamente
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
  // El costo viene en dólares (no en centavos), usar directamente
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

// Abrir portales de quejas / mantenimiento (backends FastAPI)
function openComplaintsPortal() {
  window.open('http://localhost:5007/docs', '_blank');
}

function openMaintenancePortal() {
  window.open('http://localhost:5006/docs', '_blank');
}

// Descarga de reportes desde reports-service
async function downloadReport(file: string) {
  try {
    const blob = await supportStore.downloadReport(file);
    if (!blob) throw new Error(supportStore.error || $t('profile.reportsSection.downloadError'));
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = file;
    link.click();
    URL.revokeObjectURL(link.href);
  } catch (err) {
    console.error('Error descargando reporte', err);
    alert($t('profile.reportsSection.downloadError'));
  }
}

// Enviar queja al backend movilidad sostenible
async function submitComplaint() {
  complaintError.value = "";
  complaintSuccess.value = "";

  if (!complaintForm.description.trim()) {
    complaintError.value = $t('profile.complaints.errorRequired');
    return;
  }

  const travelIdNumber = complaintForm.travelId
    ? Number(complaintForm.travelId)
    : undefined;

  if (complaintForm.travelId && Number.isNaN(travelIdNumber)) {
    complaintError.value = $t('profile.complaints.errorNumeric');
    return;
  }

  const resp = await supportStore.submitComplaint({
    description: complaintForm.description.trim(),
    type: complaintForm.type,
    travelId: travelIdNumber,
  });

  if (!resp) {
    complaintError.value = supportStore.error || $t('profile.complaints.submitError');
    return;
  }

  complaintSuccess.value = $t('profile.complaints.success', { id: resp.k_id_complaints_and_claims });
  complaintForm.description = "";
  complaintForm.type = "BICYCLE";
  complaintForm.travelId = "";
}

// Actualizar tasa de cambio dinámicamente
const updateExchangeRate = async () => {
  if (selectedCurrency.value === 'COP') {
    try {
      const rateCOP = await fetchExchangeRate('USD', 'COP', 1);
      exchangeRates.value.COP = rateCOP;
      console.log(`Tasa de cambio actualizada: 1 USD = ${rateCOP} COP`);
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
    console.warn('No se puede procesar pago sin cantidad');
    alert('Error: No se puede procesar el pago sin cantidad');
    return;
  }

  if (!uid.value) {
    console.warn('No se puede procesar pago sin UID de usuario');
    alert('Error: Usuario no identificado');
    return;
  }

  // Verificar saldo suficiente
  if (balance.value !== null && balance.value < amount) {
    alert(`Saldo insuficiente. Tu saldo actual es ${formattedBalance.value} y la multa es de ${formatCost(amount)}`);
    return;
  }

  // Confirmar pago con el usuario
  const confirmed = confirm(`¿Desea pagar la multa #${fineId} por ${formatCost(amount)}?\n\nSaldo actual: ${formattedBalance.value}\nSaldo después del pago: ${formatCost((balance.value || 0) - amount)}`);
  if (!confirmed) return;

  try {
    console.log(`Pagando multa ${fineId} de ${amount} USD`);

    // Llamar al método del store para pagar la multa
    const success = await paymentStore.payFine(fineId, uid.value, amount);

    if (!success) {
      alert(paymentStore.error || 'Error al procesar el pago de la multa');
      return;
    }

    // Recargar las multas después del pago
    await paymentStore.fetchFines(uid.value);
    fines.value = paymentStore.fines;

    // Actualizar el balance
    await fetchBalance();

    alert('¡Multa pagada exitosamente! Tu saldo ha sido actualizado.');
  } catch (error) {
    console.error('Error al pagar multa:', error);
    alert('Error al procesar el pago de la multa');
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

// Configurar listeners para actualizar balance
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

  // Listen to window resize for responsive menu
  const handleResize = () => {
    windowWidth.value = window.innerWidth;
    if (windowWidth.value >= 768) {
      mobileMenuOpen.value = false;
    }
  };
  window.addEventListener('resize', handleResize);
  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
  });
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

    [data-theme="dark"] & .currency-selector {
      label {
        color: var(--color-text-primary-dark);
      }

      .currency-btn {
        border-color: var(--color-border-dark);
        background: var(--color-surface-dark);
        color: var(--color-text-primary-dark);

        &:hover {
          border-color: var(--color-primary-light);
        }

        &.active {
          background: var(--color-primary-light);
          color: var(--color-button-text-dark);
          border-color: var(--color-primary-light);
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
  background: #2E7D32;
  color: white;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-add-balance:hover {
  background: #1f5620;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
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

[data-theme="dark"] .refresh-btn {
  color: var(--color-text-secondary-dark);
}

[data-theme="dark"] .refresh-btn:hover {
  color: var(--color-primary-light);
  background: rgba(46, 125, 50, 0.1);
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

/* Sección de balance - mantener en columna única */
.balance-section {
  grid-column: 1 / -1 !important;
}

/* Nueva parrilla de tarjetas para quejas y reportes - 2 columnas */
.cards-grid {
  display: grid !important;
  grid-template-columns: repeat(2, 1fr) !important;
  gap: 1.25rem !important;
}

.action-card {
  background: #ffffff;
  border: 1px solid #e6e8eb;
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;

  h3 {
    margin: 0;
    color: #004e61;
    font-size: 1.1rem;
  }

  p {
    margin: 0;
    color: #4a5568;
    font-size: 0.95rem;
  }
}

[data-theme="dark"] .action-card {
  background: var(--color-surface-dark);
  border-color: var(--color-border-dark);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);

  h3 {
    color: var(--color-primary-light);
  }

  p {
    color: var(--color-text-secondary-dark);
  }
}

.report-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 0.5rem;
}

.btn-primary,
.btn-secondary {
  border: none;
  border-radius: 8px;
  padding: 0.55rem 0.9rem;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.btn-primary {
  background: #2E7D32;
  color: #fff;
}

.btn-primary:hover {
  background: #1f5620;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
}

.btn-secondary {
  background: #f3f5f7;
  color: #2E7D32;
  border: 1px solid #dbe3e8;
}

.btn-secondary:hover {
  background: #e6edf2;
  border-color: #2E7D32;
}

[data-theme="dark"] .btn-secondary {
  background: rgba(46, 125, 50, 0.1);
  color: var(--color-primary-light);
  border-color: var(--color-primary-light);
}

[data-theme="dark"] .btn-secondary:hover {
  background: rgba(46, 125, 50, 0.2);
}

.muted {
  color: #6b7280;
  margin-top: 0.3rem;
  font-size: 0.95rem;
}

[data-theme="dark"] .muted {
  color: var(--color-text-secondary-dark);
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

  .profile-container {
    position: relative;
  }

  .mobile-menu-btn {
    position: fixed;
    top: 75px;
    left: 15px;
    z-index: 1000;
    background: #2E7D32;
    color: white;
    border: none;
    border-radius: 6px;
    width: 45px;
    height: 45px;
    font-size: 22px;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transition: all 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;

    &:active {
      transform: scale(0.95);
    }
  }

  [data-theme="dark"] .mobile-menu-btn {
    background: var(--color-primary-light);
    color: var(--color-button-text-dark);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }

  .profile-sidebar {
    position: fixed;
    top: 140px;
    left: -100%;
    width: 100%;
    height: auto;
    max-height: calc(100vh - 140px);
    z-index: 999;
    transition: left 0.3s ease;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
    overflow-y: auto;

    &.mobile-open {
      left: 0;
    }
  }

  .sidebar-nav {
    display: flex;
    flex-direction: column;
    gap: 0;
  }

  .sidebar-btn {
    width: 100%;
    text-align: left;
    border-left: 4px solid transparent;
    padding: 1rem;
    border-radius: 0;
  }

  .profile {
    margin-left: 0;
    padding: 1.5rem 1rem;
    margin-top: 70px;
  }

  .profile-header h1 {
    font-size: 1.5rem;
  }

  .cards-grid {
    grid-template-columns: 1fr !important;
  }

  .action-card {
    padding: 1rem;
  }

  .btn-primary,
  .btn-secondary {
    padding: 0.5rem 0.75rem;
    font-size: 0.85rem;
  }
}

@media (max-width: 480px) {
  .mobile-menu-btn {
    width: 42px;
    height: 42px;
    font-size: 20px;
    top: 70px;
    left: 12px;
  }

  .profile {
    min-height: auto;
    padding: 1rem;
    gap: 1.5rem;
    margin-top: 70px;
  }

  .profile-sidebar {
    width: 100%;
    left: -100%;
  }

  .sidebar-btn {
    padding: 0.75rem 1rem;
  }

  .profile-header h1 {
    font-size: 1.3rem;
  }

  .section-title {
    font-size: 1.2rem;
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

  .action-card {
    padding: 0.75rem;
    gap: 0.5rem;

    h3 {
      font-size: 1rem;
    }

    p {
      font-size: 0.9rem;
    }
  }

  .btn-primary,
  .btn-secondary {
    padding: 0.45rem 0.6rem;
    font-size: 0.8rem;
  }
}
</style>
