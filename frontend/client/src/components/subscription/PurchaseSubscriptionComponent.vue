<template>
  <div class="subscription-purchase">
    <!-- Header -->
    <div class="header">
      <h1>{{ $t('subscription.purchase.title') }}</h1>
      <p>{{ $t('subscription.purchase.subtitle') }}</p>
    </div>

    <!-- Información de suscripción -->
    <div class="subscription-info">
      <div class="info-card">
        <h3>📋 Plan Mensual</h3>
        <ul class="features">
          <li> 50 viajes mensuales incluidos</li>
          <li> Sin costo por minuto excedente</li>
          <li> Prioridad en reservas</li>
          <li> Soporte 24/7</li>
        </ul>
        
        <div class="price-section">
          <span class="price">{{ $t('subscription.purchase.price') }}</span>
        </div>
      </div>
    </div>

    <!-- Estado actual -->
    <div class="current-status">
      <div class="balance-info">
        <h4>{{ $t('subscription.purchase.currentBalance') }}</h4>
        <p class="balance-amount">{{ formattedBalance }}</p>
      </div>
      
      <div class="requirement-info">
        <p v-if="canSubscribe" class="success-message">
           {{ $t('subscription.purchase.eligible') }}
        </p>
        <p v-else class="warning-message">
           {{ $t('subscription.purchase.insufficientBalance') }}
        </p>
        <p class="requirement">
          {{ $t('subscription.purchase.minimumRequirement') }}
        </p>
      </div>
    </div>

    <!-- Botón de compra -->
    <button
      class="butn-primary purchase-btn"
      @click="handlePurchase"
      :disabled="loading || !canSubscribe"
    >
      <span v-if="loading">
        {{ $t('subscription.purchase.processing') }}
      </span>
      <span v-else>
        {{ $t('subscription.purchase.button') }}
      </span>
    </button>

    <!-- Mensajes de estado -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-if="purchaseSuccess" class="success-message">
      <h3>🎉 {{ $t('subscription.purchase.success.title') }}</h3>
      <p>{{ $t('subscription.purchase.success.message') }}</p>
      <div class="success-details">
        <p><strong>Plan:</strong> Mensual</p>
        <p><strong>Viajes incluidos:</strong> 50</p>
        <p><strong>Nuevo saldo:</strong> {{ formattedBalance }}</p>
      </div>
    </div>

    <!-- Información adicional -->
    <div class="additional-info">
      <p>{{ $t('subscription.purchase.note') }}</p>
      <p class="small-text">
        {{ $t('subscription.purchase.terms') }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAuth } from 'firebase/auth'
import useAuth from '@/stores/auth'
import useSubscriptionStore from '@/stores/subscription'

const { t: $t } = useI18n()

// Stores
const authStore = useAuth()
const subscriptionStore = useSubscriptionStore()

// Estado reactivo
const loading = ref(false)
const canSubscribe = ref(false)
const purchaseSuccess = ref(false)

// Computed
const formattedBalance = computed(() => subscriptionStore.formattedBalance)
const error = computed(() => subscriptionStore.error)

// Inicializar al montar
onMounted(async () => {
  await checkSubscriptionEligibility()
})

// Verificar elegibilidad
const checkSubscriptionEligibility = async () => {
  const firebaseAuth = getAuth()
  const firebaseUser = firebaseAuth.currentUser
  
  if (!firebaseUser) {
    console.warn('Usuario no autenticado')
    return
  }

  canSubscribe.value = await subscriptionStore.canSubscribe(firebaseUser.uid)
}

// Manejar compra
const handlePurchase = async () => {
  const firebaseAuth = getAuth()
  const firebaseUser = firebaseAuth.currentUser
  
  if (!firebaseUser) {
    alert($t('subscription.purchase.notAuthenticated'))
    return
  }

  loading.value = true
  purchaseSuccess.value = false
  subscriptionStore.clearError()

  try {
    const result = await subscriptionStore.purchaseMonthlySubscription({
      userId: firebaseUser.uid,
      token: authStore.token || undefined
    })

    if (result) {
      purchaseSuccess.value = true
      // Actualizar estado de elegibilidad
      await checkSubscriptionEligibility()
    } else {
      alert($t('subscription.purchase.error'))
    }
  } catch (error) {
    console.error('Error en compra de suscripción:', error)
    alert($t('subscription.purchase.error'))
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.subscription-purchase {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.header {
  text-align: center;
  margin-bottom: 32px;
  
  h1 {
    font-size: 28px;
    color: #333;
    margin-bottom: 8px;
  }
  
  p {
    color: #666;
    font-size: 16px;
  }
}

.subscription-info {
  margin-bottom: 32px;
  
  .info-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 24px;
    border-radius: 12px;
    
    h3 {
      font-size: 20px;
      margin-bottom: 16px;
    }
    
    .features {
      list-style: none;
      padding: 0;
      margin-bottom: 24px;
      
      li {
        padding: 8px 0;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        
        &:last-child {
          border-bottom: none;
        }
      }
    }
    
    .price-section {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .price {
        font-size: 12px;
        opacity: 0.8;
      }
      
      .original-price {
        font-size: 32px;
        font-weight: bold;
      }
      
      .converted-price {
        font-size: 14px;
        opacity: 0.9;
      }
    }
  }
}

.current-status {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  
  .balance-info {
    text-align: center;
    margin-bottom: 16px;
    
    h4 {
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
    }
    
    .balance-amount {
      font-size: 32px;
      font-weight: bold;
      color: #2e7d32;
    }
  }
  
  .requirement-info {
    text-align: center;
    
    .success-message {
      color: #2e7d32;
      font-weight: 500;
      margin-bottom: 8px;
    }
    
    .warning-message {
      color: #f57c00;
      font-weight: 500;
      margin-bottom: 8px;
    }
    
    .requirement {
      font-size: 14px;
      color: #666;
    }
  }
}

.purchase-btn {
  width: 100%;
  padding: 16px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 24px;
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 16px;
  text-align: center;
}

.success-message {
  background: #e8f5e9;
  color: #2e7d32;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  text-align: center;
  
  h3 {
    margin-top: 0;
    margin-bottom: 12px;
  }
  
  .success-details {
    background: white;
    padding: 16px;
    border-radius: 8px;
    margin-top: 16px;
    text-align: left;
    
    p {
      margin: 8px 0;
    }
  }
}

.additional-info {
  text-align: center;
  color: #666;
  font-size: 14px;
  
  .small-text {
    font-size: 12px;
    opacity: 0.7;
    margin-top: 8px;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .subscription-purchase {
    padding: 16px;
    border-radius: 12px;
  }
  
  .header h1 {
    font-size: 24px;
  }
  
  .info-card {
    padding: 16px !important;
    
    .original-price {
      font-size: 24px !important;
    }
  }
  
  .balance-amount {
    font-size: 24px !important;
  }
}
</style>