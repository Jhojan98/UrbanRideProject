<template>
  <section class="balance-card">
    <div class="balance-header">
      <h2>{{ $t('profile.balance.title') }}</h2>
      <div class="currency-selector">
        <label for="currency-select">{{ $t('profile.balance.currency') }}:</label>
        <select
          id="currency-select"
          v-model="localCurrency"
          class="currency-select"
          @change="$emit('currency-change', localCurrency)"
        >
          <option value="USD">USD - {{ $t('balance.currencies.USD') }}</option>
          <option value="COP">COP - {{ $t('balance.currencies.COP') }}</option>
          <option value="EUR">EUR - {{ $t('balance.currencies.EUR') }}</option>
        </select>
      </div>
    </div>

    <div class="balance-display">
      <span class="balance-label">{{ $t('profile.balance.currentBalance') }}</span>
      <span class="balance-value">
        {{ formattedBalance }}
        <button @click="$emit('refresh')" class="refresh-btn" :disabled="isLoading">
          ⟳
        </button>
        <span v-if="isLoading" class="loading-spinner">↻</span>
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
      <button class="btn-add-balance" @click="$emit('add-balance')">
        {{ $t('profile.balance.addBalance') }}
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

interface Props {
  selectedCurrency: 'USD' | 'COP' | 'EUR';
  formattedBalance: string;
  isLoading: boolean;
}

const props = defineProps<Props>();
const localCurrency = ref<Props['selectedCurrency']>(props.selectedCurrency);

// Keep local select in sync if parent changes programmatically
watch(() => props.selectedCurrency, (val) => {
  localCurrency.value = val;
});
</script>

<style scoped>
.balance-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.balance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.currency-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.currency-select {
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #d7d7d7;
}

.balance-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.balance-label {
  font-weight: 600;
  color: #444;
}

.balance-value {
  font-size: 1.4rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 6px;
}

.refresh-btn {
  background: none;
  border: none;
  color: #555;
  cursor: pointer;
  font-size: 14px;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.refresh-btn:hover {
  color: #0074d4;
  background: #f0f4ff;
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-spinner {
  display: inline-block;
  animation: spin 1s linear infinite;
}

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.card-info h4 { margin: 0 0 6px 0; }
.card-details { display: flex; gap: 10px; color: #555; font-size: 14px; }

.payment-actions { display: flex; justify-content: flex-start; }

.btn-add-balance {
  background: #0074d4;
  color: white;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: background 0.3s;
}

.btn-add-balance:hover { background: #0056a3; }

@media (max-width: 768px) {
  .balance-header { flex-direction: column; align-items: flex-start; }
  .currency-selector { width: 100%; }
  .balance-card { padding: 12px; }
}
</style>
