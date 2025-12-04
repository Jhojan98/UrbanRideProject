<template>
  <div class="payment-result container">
    <div class="card">
      <h1>Pago realizado ✅</h1>
      <p>¡Gracias por tu compra! Tu saldo ha sido recargado exitosamente.</p>
      
      <div class="details" v-if="sessionId || uid">
        <p><strong>Session ID:</strong> {{ sessionId }}</p>
        <p v-if="uid"><strong>Usuario:</strong> {{ uid }}</p>
      </div>

      <div class="balance-info" v-if="newBalance !== null">
        <p><strong>Saldo actualizado:</strong> {{ formatBalance(newBalance) }}</p>
      </div>
      
      <div class="actions">
        <router-link to="/my-profile" class="btn btn-primary" @click="markPaymentComplete">
          Ver mi saldo actualizado
        </router-link>
        <router-link to="/" class="btn btn-secondary">
          Volver al inicio
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
// CORRECCIÓN: Importar getAuth de Firebase
import { getAuth } from 'firebase/auth';

export default {
  name: "PaymentSuccessComponent",
  data() {
    return {
      sessionId: null,
      uid: null,
      newBalance: null
    };
  },
  created() {
    // Leer query params que Stripe agrega
    this.sessionId = this.$route.query.session_id || null;
    
    // Obtener UID del usuario actual
    const auth = getAuth(); // Ahora getAuth está definido
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
      // Establecer marca de tiempo del último pago
      localStorage.setItem('last_payment_time', Date.now().toString());
      
      // Establecer bandera para actualizar balance
      localStorage.setItem('should_refresh_balance', 'true');
      
      console.log("Pago marcado como completado, balance se actualizará");
    },
    
    async fetchUpdatedBalance() {
      if (!this.uid) return;
      
      try {
        // Intentar obtener el balance actualizado del usuario-service
        const response = await fetch(`http://localhost:8001/balance/${this.uid}`, {
          method: 'GET',
          headers: {
            'Accept': 'application/json'
          }
        });
        
        if (response.ok) {
          const data = await response.json();
          this.newBalance = data.balance || 0;
          
          // Actualizar localStorage con el nuevo balance
          localStorage.setItem("userBalance", this.newBalance.toString());
        }
      } catch (error) {
        console.error("Error obteniendo balance actualizado:", error);
      }
    },
    
    formatBalance(balance) {
      return new Intl.NumberFormat("es-CO", { 
        style: "currency", 
        currency: "COP",
        minimumFractionDigits: 0 
      }).format(balance);
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