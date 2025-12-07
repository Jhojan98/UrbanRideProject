<template>
  <div class="payment-result container">
    <div class="card">
      <h1>{{ $t('payments.success.title') }}</h1>
      <p>{{ $t('payments.success.subtitle') }}</p>

      <div class="details" v-if="sessionId || uid">
        <p><strong>{{ $t('payments.success.sessionId') }}</strong> {{ sessionId }}</p>
        <p v-if="uid"><strong>{{ $t('payments.success.user') }}</strong> {{ uid }}</p>
      </div>

      <div class="currency-selector" v-if="newBalance !== null">
        <label>{{ $t('payments.recharge.currency') }}:</label>
        <button
          v-for="curr in currencies"
          :key="curr"
          @click="selectedCurrency = curr"
          :class="['currency-btn', { 'active': selectedCurrency === curr }]"
        >
          {{ curr }}
        </button>
      </div>

      <div class="balance-info" v-if="newBalance !== null">
        <p><strong>{{ $t('payments.success.updatedBalance') }}</strong> {{ formatBalance(newBalance) }}</p>
      </div>

      <div class="actions">
        <router-link to="/my-profile" class="btn btn-primary" @click="markPaymentComplete">
          {{ $t('payments.success.viewBalance') }}
        </router-link>
        <router-link to="/" class="btn btn-secondary">
          {{ $t('payments.success.backHome') }}
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { getAuth } from 'firebase/auth';
import usePaymentStore from '@/stores/payment';
import { fetchExchangeRate } from '@/services/currencyExchange';

export default {
  name: "PaymentSuccessComponent",
  inject: {
    t: {
      from: '$t',
      default: (key) => key,
    },
  },
  data() {
    return {
      sessionId: null,
      uid: null,
      newBalance: null,
      paymentStore: usePaymentStore(),
      currencies: ['USD', 'COP'],
      selectedCurrency: 'COP',
      exchangeRate: 4000 // Valor por defecto
    };
  },
  watch: {
    selectedCurrency(newCurrency) {
      if (newCurrency === 'COP' && this.newBalance !== null) {
        this.updateExchangeRate();
      }
    }
  },
  created() {
    // Leer query params que Stripe agrega
    this.sessionId = this.$route.query.session_id || null;

    // Obtener UID del usuario actual
    const auth = getAuth();
    const user = auth.currentUser;
    if (user) {
      this.uid = user.uid;
    } else {
      // Intentar obtener de localStorage como fallback
      this.uid = localStorage.getItem("uid");
    }

    // Marcar que el pago se completó
    this.markPaymentComplete();

    // Intentar obtener el nuevo balance
    this.fetchUpdatedBalance();
  },
  methods: {
    markPaymentComplete() {
      this.paymentStore.markPaymentComplete();
      console.log("Pago marcado como completado, balance se actualizará");
    },

    async fetchUpdatedBalance() {
      if (!this.uid) return;

      try {
        const result = await this.paymentStore.fetchBalance(this.uid);
        this.newBalance = result ?? 0;

        if (result !== null) {
          localStorage.setItem("userBalance", this.newBalance.toString());
          console.log("Balance actualizado desde el store:", this.newBalance);
          // Cargar tasa de cambio después de obtener el balance
          if (this.selectedCurrency === 'COP') {
            await this.updateExchangeRate();
          }
        }
      } catch (error) {
        console.error("Error obteniendo balance actualizado:", error);
      }
    },

    async updateExchangeRate() {
      if (this.selectedCurrency === 'COP') {
        try {
          const rate = await fetchExchangeRate('USD', 'COP', 1);
          this.exchangeRate = rate;
          console.log(`Tasa de cambio actualizada: 1 USD = ${rate} COP`);
        } catch (error) {
          console.error('Error obteniendo tasa de cambio:', error);
          this.exchangeRate = 4000; // Fallback
        }
      }
    },

    formatBalance(balance) {
      // El balance en DB está en centavos de USD, convertir a USD primero
      const balanceInUSD = balance / 100;

      if (this.selectedCurrency === 'COP') {
        const balanceInCOP = balanceInUSD * this.exchangeRate;
        return new Intl.NumberFormat("es-CO", {
          style: "currency",
          currency: "COP",
          minimumFractionDigits: 0
        }).format(balanceInCOP);
      } else {
        return new Intl.NumberFormat("en-US", {
          style: "currency",
          currency: "USD",
          minimumFractionDigits: 2
        }).format(balanceInUSD);
      }
    }
  }
};
</script>

<style scoped>
.payment-result {
  padding: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.card {
  max-width: 600px;
  width: 100%;
  text-align: center;
  padding: 2.5rem;
  border-radius: 1rem;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
  background: #fff;
}

h1 {
  color: #2e7d32;
  margin-bottom: 1rem;
  font-size: 2rem;
}

p {
  color: #666;
  font-size: 1.1rem;
  line-height: 1.5;
  margin-bottom: 1.5rem;
}

.details {
  text-align: left;
  margin: 1.5rem 0;
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid #e9ecef;
}

.currency-selector {
  margin: 1.5rem 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.currency-selector label {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.currency-btn {
  padding: 0.5rem 1.2rem;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s;
}

.currency-btn:hover {
  border-color: #2e7d32;
}

.currency-btn.active {
  background: #2e7d32;
  color: white;
  border-color: #2e7d32;
}

.balance-info {
  background: linear-gradient(135deg, #2e7d32 0%, #4caf50 100%);
  color: white;
  padding: 1rem;
  border-radius: 0.5rem;
  margin: 1.5rem 0;
}

.balance-info p {
  color: white;
  font-size: 1.2rem;
  margin: 0;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 2rem;
}

.btn {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  text-decoration: none;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
  text-align: center;
}

.btn-primary {
  background: #0074d4;
  color: white;
}

.btn-primary:hover {
  background: #0056a3;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 116, 212, 0.3);
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 768px) {
  .payment-result {
    padding: 1rem;
  }

  .card {
    padding: 1.5rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  .actions {
    flex-direction: column;
  }
}
</style>
