<template>
  <div class="profile">
    <div class="profile-header">
      <h1>{{ welcomeText }}</h1>
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

      <!-- Sección de Saldo y Tarjeta (Centrada) -->
      <section class="profile-section balance-section">
        <h2 class="section-title">{{ $t('profile.balance.title') }}</h2>
        <div class="balance-card centered-card">
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
              Añadir saldo
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { getAuth, onAuthStateChanged, User } from "firebase/auth";

const router = useRouter();
const { t: $t } = useI18n();

// Datos del usuario
const uid = ref<string | null>(null);
const userName = ref<string | null>(null);
const balance = ref<number | null>(null);
const isLoadingBalance = ref(false);

// Historial de viajes (datos de ejemplo)
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

// Texto de bienvenida
const welcomeText = computed(() => {
  const name = userName.value ?? "CLIENTE";
  return `Bienvenido de nuevo, ${name}`;
});

// Formato de balance
const formattedBalance = computed(() => {
  if (balance.value === null) return "--";
  return new Intl.NumberFormat("es-CO", { 
    style: "currency", 
    currency: "COP",
    minimumFractionDigits: 0 
  }).format(balance.value);
});

// Navegar a PaymentMethods
function goToPaymentMethods() {
  router.push({ name: "payment-methods" });
}

// Obtener uid y nombre desde Firebase
function attachFirebaseAuthListener() {
  const auth = getAuth();
  onAuthStateChanged(auth, (user: User | null) => {
    if (user) {
      uid.value = user.uid;
      userName.value = user.displayName ?? (user.email ? user.email.split("@")[0] : `user_${user.uid.substring(0,6)}`);
      
      try {
        localStorage.setItem("uid", uid.value);
        localStorage.setItem("userName", userName.value);
      } catch (e) { /* ignore storage errors */ }
      
      fetchBalance();
    } else {
      const storedUid = localStorage.getItem("uid");
      const storedName = localStorage.getItem("userName");
      if (storedUid) {
        uid.value = storedUid;
        userName.value = storedName ?? "CLIENTE";
        fetchBalance();
      } else {
        uid.value = null;
        userName.value = "CLIENTE";
        balance.value = null;
      }
    }
  });
}

// Obtener balance del usuario-service
async function fetchBalance() {
  if (!uid.value) return;
  
  isLoadingBalance.value = true;
  try {
    // Llama directamente al usuario-service (puerto 8001)
    const response = await fetch(`http://localhost:8001/balance/${uid.value}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });
    
    if (response.ok) {
      const data = await response.json();
      balance.value = data.balance || 0;
      console.log("Balance obtenido desde usuario-service:", balance.value);
      
      // Guardar en localStorage como caché
      localStorage.setItem("userBalance", (balance.value ?? 0).toString());
    } else {
      console.error("Error obteniendo balance:", response.status);
      // Intentar obtener de localStorage como fallback
      const storedBalance = localStorage.getItem("userBalance");
      balance.value = storedBalance ? parseInt(storedBalance, 10) : 0;
    }
  } catch (error) {
    console.error("Error en fetchBalance:", error);
    const storedBalance = localStorage.getItem("userBalance");
    balance.value = storedBalance ? parseInt(storedBalance, 10) : 0;
  } finally {
    isLoadingBalance.value = false;
  }
}

// Refrescar balance manualmente
async function refreshBalance() {
  await fetchBalance();
}

// Configurar listeners para actualizar el balance
function setupBalanceListeners() {
  // Definir la función para el event listener
  const handleFocus = () => {
    console.log("Ventana enfocada, actualizando balance...");
    fetchBalance();
  };
  
  // Escuchar evento cuando la ventana se enfoca (por si el usuario vuelve del pago)
  window.addEventListener('focus', handleFocus);
  
  // Verificar si hay un pago reciente
  const lastPaymentTime = localStorage.getItem('last_payment_time');
  if (lastPaymentTime) {
    const now = Date.now();
    const paymentTime = parseInt(lastPaymentTime, 10);
    // Si el pago fue hace menos de 5 minutos, forzar actualización
    if (now - paymentTime < 5 * 60 * 1000) {
      console.log("Pago reciente detectado, forzando actualización de saldo");
      fetchBalance();
      localStorage.removeItem('last_payment_time');
    }
  }
  
  // Actualizar balance periódicamente (cada 30 segundos)
  const intervalId = setInterval(() => {
    if (uid.value && document.visibilityState === 'visible') {
      fetchBalance();
    }
  }, 30000);
  
  // Limpiar intervalo y event listener al desmontar
  onUnmounted(() => {
    clearInterval(intervalId);
    window.removeEventListener('focus', handleFocus); // CORRECCIÓN: Usar la misma referencia
  });
}

onMounted(() => {
  attachFirebaseAuthListener();
  setupBalanceListeners();
  
  // También forzar actualización cuando se monta el componente
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
  .balance-section .centered-card {
    max-width: 100%;
    padding: 0 10px;
  }
}
</style>