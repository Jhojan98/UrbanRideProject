<template>
  <section class="dashboard-card">
    <header class="dashboard-header">
      <h3>{{ $t('profile.fines.title') }}</h3>
      <span v-if="loading" class="badge muted">{{ $t('profile.loading') }}</span>
    </header>

    <div v-if="loading" class="loading-message">{{ $t('profile.loading') }}</div>
    <div v-else-if="fines.length === 0" class="empty-message">{{ $t('profile.fines.noFines') }}</div>
    <div v-else class="fines-list">
      <div
        v-for="fine in fines"
        :key="fine.k_user_fine"
        class="fine-card"
        :class="{ 'fine-paid': fine.t_state === 'PAID' }"
      >
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
          <button class="btn-pay-fine" @click="emitPay(fine.k_user_fine, fine.v_amount_snapshot)">
            {{ $t('profile.fines.payNow') }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type Fine from '@/models/Fine';

interface Props {
  fines: Fine[];
  loading: boolean;
}

defineProps<Props>();
const emit = defineEmits<{
  (e: 'pay', fineId: number, amount: number | undefined): void;
}>();

const formatDate = (date: string | number | Date | undefined): string => {
  if (!date) return 'N/A';
  const dateObj = typeof date === 'string' || typeof date === 'number' ? new Date(date) : date;
  return dateObj.toLocaleDateString('es-CO', { year: 'numeric', month: 'short', day: 'numeric' });
};

const formatCost = (cost: number | undefined): string => {
  if (cost === null || cost === undefined) return 'N/A';
  const costInUSD = typeof cost === 'number' ? cost : 0;
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2
  }).format(costInUSD);
};

const emitPay = (fineId: number, amount: number | undefined) => {
  emit('pay', fineId, amount);
};
</script>

<style scoped>
.dashboard-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.dashboard-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.badge.muted { color: #666; font-size: 13px; }
.loading-message, .empty-message { padding: 10px; color: #555; }

.fines-list { display: grid; gap: 12px; }

.fine-card {
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 12px;
  background: #fafbff;
}
.fine-card.fine-paid { border-color: #d7ffd9; background: #f6fff7; }

.fine-header { display: flex; justify-content: space-between; align-items: center; }
.fine-status { padding: 4px 10px; border-radius: 8px; font-size: 12px; font-weight: 600; }
.fine-status.paid { background: #d7ffd9; color: #1b7a2f; }
.fine-status.pending { background: #fff4d6; color: #a06200; }

.fine-details p { margin: 4px 0; color: #444; }

.fine-actions { margin-top: 8px; display: flex; justify-content: flex-end; }
.btn-pay-fine {
  background: #0074d4;
  color: white;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}
.btn-pay-fine:hover { background: #005fa8; }

@media (max-width: 768px) {
  .dashboard-card { padding: 12px; }
  .fine-header { flex-direction: column; align-items: flex-start; gap: 6px; }
}
</style>
