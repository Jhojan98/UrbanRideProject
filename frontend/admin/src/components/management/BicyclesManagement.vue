<template>
  <div class="section">
    <div class="section-header">
      <h2>{{ t('management.bicycles.title') }}</h2>
      <button class="btn-primary" @click="showForm = true">
        <span class="material-symbols-outlined">add</span>
        {{ t('management.bicycles.create') }}
      </button>
    </div>

    <div v-if="showForm" class="form-card">
      <h3>{{ t('management.bicycles.newBicycle') }}</h3>
      <form @submit.prevent="handleCreate">
        <div class="form-group">
          <label>{{ t('management.bicycles.type') }}</label>
          <select v-model="form.type" required>
            <option value="ELECTRIC">{{ t('management.bicycles.electric') }}</option>
            <option value="MECHANIC">{{ t('management.bicycles.mechanic') }}</option>
          </select>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>{{ t('management.bicycles.series') }}</label>
            <input v-model.number="form.series" type="number" required />
          </div>
          <div class="form-group">
            <label>{{ t('management.bicycles.padlockStatus') }}</label>
            <select v-model="form.padlockStatus" required>
              <option value="LOCKED">LOCKED</option>
              <option value="UNLOCKED">UNLOCKED</option>
            </select>
          </div>
        </div>
        <div v-if="form.type === 'ELECTRIC'" class="form-group">
          <label>{{ t('management.bicycles.battery') }}</label>
          <input v-model.number="form.battery" type="number" min="0" max="100" required />
        </div>
        <div class="form-actions">
          <button type="submit" class="btn-primary" :disabled="bicycleStore.loading">
            {{ bicycleStore.loading ? t('common.saving') : t('common.save') }}
          </button>
          <button type="button" class="btn-secondary" @click="showForm = false">
            {{ t('common.cancel') }}
          </button>
        </div>
      </form>
    </div>

    <div class="data-table">
      <table v-if="bicycleStore.bicycles.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>{{ t('management.bicycles.series') }}</th>
            <th>{{ t('management.bicycles.model') }}</th>
            <th>{{ t('management.bicycles.padlockStatus') }}</th>
            <th>{{ t('management.bicycles.battery') }}</th>
            <th>{{ t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="bicycle in bicycleStore.bicycles" :key="bicycle.idBicycle">
            <td><code>{{ bicycle.idBicycle }}</code></td>
            <td>{{ bicycle.series }}</td>
            <td>
              <span :class="['model-badge', bicycle.model.toLowerCase()]">
                {{ bicycle.model }}
              </span>
            </td>
            <td>
              <span :class="['status-badge', (bicycle.padlockStatus || bicycle.lockStatus || '').toLowerCase()]">
                {{ bicycle.padlockStatus || bicycle.lockStatus }}
              </span>
            </td>
            <td>
              <span v-if="bicycle.model === 'ELECTRIC'">{{ bicycle.battery }}%</span>
              <span v-else>N/A</span>
            </td>
            <td>
              <button class="btn-danger btn-sm" @click="handleDelete(bicycle.idBicycle || bicycle.id || '')">
                <span class="material-symbols-outlined">delete</span>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty-message">{{ t('management.bicycles.empty') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useBicycleStore } from '@/stores/bicycleStore';

const { t } = useI18n();
const bicycleStore = useBicycleStore();

const showForm = ref(false);
const form = ref({
  type: 'ELECTRIC',
  series: new Date().getFullYear(),
  padlockStatus: 'UNLOCKED',
  battery: 100,
});

async function handleCreate() {
  try {
    if (form.value.type === 'ELECTRIC') {
      await bicycleStore.createElectricBicycle({
        series: form.value.series,
        padlockStatus: form.value.padlockStatus,
        battery: form.value.battery,
      });
    } else {
      await bicycleStore.createMechanicBicycle({
        series: form.value.series,
        padlockStatus: form.value.padlockStatus,
      });
    }
    showForm.value = false;
    form.value = {
      type: 'ELECTRIC',
      series: new Date().getFullYear(),
      padlockStatus: 'UNLOCKED',
      battery: 100,
    };
    alert(t('management.bicycles.createSuccess'));
  } catch (error) {
    alert(t('management.bicycles.createError'));
  }
}

async function handleDelete(id: string) {
  if (confirm(t('management.bicycles.confirmDelete'))) {
    try {
      await bicycleStore.deleteBicycle(id);
      alert(t('management.bicycles.deleteSuccess'));
    } catch (error) {
      alert(t('management.bicycles.deleteError'));
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/management-shared-styles.scss';

code {
  background: var(--color-background-soft);
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.9rem;
}

.model-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;

  &.electric {
    background: #10b981;
    color: white;
  }

  &.mechanic {
    background: #6b7280;
    color: white;
  }
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;

  &.locked {
    background: #d1fae5;
    color: #065f46;
  }

  &.unlocked {
    background: #fef3c7;
    color: #92400e;
  }
}
</style>
