<template>
  <div class="payment-methods">
    <div class="header">
      <h1>{{ $t('payments.title') }}</h1>
      <p>{{ $t('payments.subtitle') }}</p>
    </div>
    
    <div class="payment-list">
      <div 
        v-for="method in paymentMethods" 
        :key="method.id"
        class="payment-card"
        :class="{ 'default': method.isDefault }"
      >
        <div class="card-info">
          <div class="card-type">
            <span class="card-icon">{{ getCardIcon(method.type) }}</span>
            <span class="card-name">{{ method.type }}</span>
            <span v-if="method.isDefault" class="default-badge">{{ $t('payments.primary') }}</span>
          </div>
          <div class="card-details">
            <span class="card-number">**** **** **** {{ method.lastFour }}</span>
            <span class="card-expiry">Exp: {{ method.expiry }}</span>
          </div>
        </div>
        <div class="card-actions">
          <button 
            v-if="!method.isDefault"
            class="btn-secondary"
            @click="setAsDefault(method.id)"
          >
            {{ $t('payments.makePrimary') }}
          </button>
          <button 
            class="btn-danger"
            @click="deleteMethod(method.id)"
          >
            {{ $t('payments.delete') }}
          </button>
        </div>
      </div>
    </div>
    
    <div class="add-payment-section">
      <h3>{{ $t('payments.addNewTitle') }}</h3>
      
      <form @submit.prevent="addPaymentMethod" class="payment-form">
        <div class="form-group">
          <label class="label">{{ $t('payments.cardType') }}</label>
          <select v-model="newPayment.type" class="form-select" required>
            <option value="">{{ $t('payments.selectType') }}</option>
            <option value="Visa">{{ $t('payments.visa') }}</option>
            <option value="Mastercard">{{ $t('payments.mastercard') }}</option>
            <option value="American Express">{{ $t('payments.amex') }}</option>
          </select>
        </div>
        
        <div class="form-group">
          <label class="label">{{ $t('payments.cardNumber') }}</label>
          <input 
            v-model="newPayment.cardNumber"
            type="text"
            placeholder="1234 5678 9012 3456"
            class="form-input"
            maxlength="19"
            required
          >
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label class="label">{{ $t('payments.expiry') }}</label>
            <input 
              v-model="newPayment.expiry"
              type="text"
              placeholder="MM/AA"
              class="form-input"
              maxlength="5"
              required
            >
          </div>
          
          <div class="form-group">
            <label class="label">{{ $t('payments.cvv') }}</label>
            <input 
              v-model="newPayment.cvv"
              type="text"
              placeholder="123"
              class="form-input"
              maxlength="3"
              required
            >
          </div>
        </div>
        
        <div class="form-group">
          <label class="checkbox-label">
            <input 
              type="checkbox" 
              v-model="newPayment.isDefault"
              class="checkbox"
            >
            <span>{{ $t('payments.setAsPrimary') }}</span>
          </label>
        </div>
        
        <button type="submit" class="butn-primary" :disabled="loading">
          {{ loading ? $t('payments.adding') : $t('payments.addCard') }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n';

interface PaymentMethod {
  id: string
  type: string
  lastFour: string
  expiry: string
  isDefault: boolean
}

const loading = ref(false)

// Datos de ejemplo
const paymentMethods = ref<PaymentMethod[]>([
  {
    id: '1',
    type: 'Visa',
    lastFour: '4235',
    expiry: '12/24',
    isDefault: true
  },
  {
    id: '2',
    type: 'Mastercard',
    lastFour: '5678',
    expiry: '08/25',
    isDefault: false
  }
])

const newPayment = reactive({
  type: '',
  cardNumber: '',
  expiry: '',
  cvv: '',
  isDefault: false
})

const getCardIcon = (type: string) => {
  const icons: { [key: string]: string } = {
    'Visa': '💳',
    'Mastercard': '💳',
    'American Express': '💳'
  }
  return icons[type] || '💳'
}

const formatCardNumber = (value: string) => {
  return value.replace(/\s/g, '').replace(/(\d{4})/g, '$1 ').trim()
}

const addPaymentMethod = async () => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    // Simula llamada a API
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const newMethod: PaymentMethod = {
      id: Date.now().toString(),
      type: newPayment.type,
      lastFour: newPayment.cardNumber.slice(-4),
      expiry: newPayment.expiry,
      isDefault: newPayment.isDefault || paymentMethods.value.length === 0
    }
    
    //marcar como principal, quitar principal de otros
    if (newPayment.isDefault) {
      paymentMethods.value.forEach(method => {
        method.isDefault = false
      })
    }
    
    paymentMethods.value.push(newMethod)
    
    // Reset form
    newPayment.type = ''
    newPayment.cardNumber = ''
    newPayment.expiry = ''
    newPayment.cvv = ''
    newPayment.isDefault = false
    
  } catch (error) {
    console.error('Error adding payment method:', error)
  } finally {
    loading.value = false
  }
}

const setAsDefault = (methodId: string) => {
  paymentMethods.value.forEach(method => {
    method.isDefault = method.id === methodId
  })
}

const { t: $t } = useI18n();
const deleteMethod = (methodId: string) => {
  if (confirm($t('payments.confirmDelete') as string)) {
    const methodToDelete = paymentMethods.value.find(m => m.id === methodId)
    
    // Si es el método principal, asignar otro como principal
    if (methodToDelete?.isDefault && paymentMethods.value.length > 1) {
      const otherMethod = paymentMethods.value.find(m => m.id !== methodId)
      if (otherMethod) {
        otherMethod.isDefault = true
      }
    }
    
    paymentMethods.value = paymentMethods.value.filter(m => m.id !== methodId)
  }
}
</script>

<style lang="scss" scoped>
.payment-methods {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.header {
  margin-bottom: 30px;
  
  h1 {
    margin: 0 0 8px 0;
    color: #333;
  }
  
  p {
    margin: 0;
    color: #666;
  }
}

.payment-list {
  margin-bottom: 40px;
}

.payment-card {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
  
  &.default {
    border-color: #007bff;
    background: #f8fbff;
  }
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  }
}

.card-info {
  flex: 1;
}

.card-type {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  
  .card-icon {
    margin-right: 8px;
    font-size: 20px;
  }
  
  .card-name {
    font-weight: 600;
    color: #333;
    margin-right: 12px;
  }
  
  .default-badge {
    background: #007bff;
    color: white;
    padding: 2px 8px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
  }
}

.card-details {
  display: flex;
  gap: 20px;
  
  .card-number {
    color: #666;
    font-family: monospace;
  }
  
  .card-expiry {
    color: #666;
  }
}

.card-actions {
  display: flex;
  gap: 10px;
}

.add-payment-section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  
  h3 {
    margin: 0 0 20px 0;
    color: #333;
  }
}

.payment-form {
  .form-group {
    margin-bottom: 20px;
  }
  
  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 15px;
  }
}

.form-select,
.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  
  &:focus {
    outline: none;
    border-color: #007bff;
  }
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  
  .checkbox {
    margin-right: 8px;
  }
}

.btn-secondary {
  background: #6c757d;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  
  &:hover {
    background: #5a6268;
  }
}

.btn-danger {
  background: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  
  &:hover {
    background: #c82333;
  }
}

.butn-primary:disabled {
  background: #6c757d;
  cursor: not-allowed;
}
</style>