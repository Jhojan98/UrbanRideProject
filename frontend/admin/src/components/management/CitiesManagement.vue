<template>
  <div class="section">
    <div class="section-header">
      <h2>{{ t('management.cities.title') }}</h2>
      <button class="btn-primary" @click="showForm = true">
        <span class="material-symbols-outlined">add</span>
        {{ t('management.cities.create') }}
      </button>
    </div>

    <div v-if="showForm" class="form-card">
      <h3>{{ t('management.cities.newCity') }}</h3>
      <form @submit.prevent="handleCreate">
        <div class="form-group">
          <label>{{ t('management.cities.idCity') }}</label>
          <input v-model.number="form.idCity" type="number" required />
        </div>
        <div class="form-group">
          <label>{{ t('management.cities.cityName') }}</label>
          <input v-model="form.cityName" type="text" required />
        </div>
        <div class="form-actions">
          <button type="submit" class="btn-primary">{{ t('common.save') }}</button>
          <button type="button" class="btn-secondary" @click="showForm = false">
            {{ t('common.cancel') }}
          </button>
        </div>
      </form>
    </div>

    <div class="data-table">
      <table v-if="cityStore.cities.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>{{ t('management.cities.cityName') }}</th>
            <th>{{ t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="city in cityStore.cities" :key="city.idCity">
            <td>{{ city.idCity }}</td>
            <td>{{ city.cityName }}</td>
            <td>
              <button class="btn-danger btn-sm" @click="handleDelete(city.idCity)">
                <span class="material-symbols-outlined">delete</span>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty-message">{{ t('management.cities.empty') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useCityStore } from '@/stores/cityStore';

const { t } = useI18n();
const cityStore = useCityStore();

const showForm = ref(false);
const form = ref({
  idCity: 0,
  cityName: '',
});

async function handleCreate() {
  try {
    await cityStore.createCity(form.value.idCity, form.value.cityName);
    showForm.value = false;
    form.value = { idCity: 0, cityName: '' };
    alert(t('management.cities.createSuccess'));
  } catch (error) {
    alert(t('management.cities.createError'));
  }
}

async function handleDelete(id: number) {
  if (confirm(t('management.cities.confirmDelete'))) {
    try {
      await cityStore.deleteCity(id);
      alert(t('management.cities.deleteSuccess'));
    } catch (error) {
      alert(t('management.cities.deleteError'));
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/management-shared-styles.scss';
</style>