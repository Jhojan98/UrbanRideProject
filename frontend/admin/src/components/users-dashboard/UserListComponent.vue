<template>
  <div class="user-list-container">
    <div class="header">
      <h1>{{ t('users.list.title') }}</h1>
      <div class="search-box">
        <input
          v-model="searchQuery"
          type="text"
          :placeholder="t('users.list.searchPlaceholder')"
          class="search-input"
        />
      </div>
    </div>

    <div class="table-container">
      <table class="users-table">
        <thead>
          <tr>
            <th>{{ t('users.list.tableHeaders.user') }}</th>
            <th>{{ t('users.list.tableHeaders.email') }}</th>
            <th>{{ t('users.list.tableHeaders.subscription') }}</th>
            <th>{{ t('users.list.tableHeaders.balance') }}</th>
            <th>{{ t('users.list.tableHeaders.registrationDate') }}</th>
            <th>{{ t('users.list.tableHeaders.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="user in filteredUsers"
            :key="user.uidUser"
            @click="selectUser(user)"
            :class="{ 'selected': selectedUser?.uidUser === user.uidUser }"
            class="user-row"
          >
            <td>{{ user.userName }}</td>
            <td>{{ user.email }}</td>
            <td>
              <span :class="['badge', `badge-${getSubscriptionClass(user.subscriptionType)}`]">
                {{ user.subscriptionType ?? t('users.list.noSubscription') }}
              </span>
            </td>
            <td>${{ user.balance?.toLocaleString('es-ES') ?? 0 }}</td>
            <td>{{ user.timestamp ? formatDate(user.timestamp) : '' }}</td>
            <td>
              <button
                @click.stop="selectUser(user)"
                class="btn-view"
              >
                {{ t('users.list.btnView') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredUsers.length === 0" class="no-data">
        <p>{{ t('users.list.noData') }}</p>
      </div>
    </div>

    <!-- Modal de historial de viajes -->
    <UserDetailsModal
      v-if="selectedUser"
      :user="selectedUser"
      :travels="userTravels"
      :fines="userFines"
      :cacs="userCaCs"
      :loading="loadingDetails"
      @close="closeModal"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import type { Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import usersStore from '@/stores/userStore'
import type User from '@/models/User'
import type Travel from '@/models/Travel'
import type Fine from '@/models/Fine'
import type CaC from '@/models/CaC'
import UserDetailsModal from './UserDetailsModal.vue'

const { t } = useI18n()
const userStore = usersStore()

const selectedUser: Ref<User | null> = ref(null)
const searchQuery = ref('')
const loadingDetails = ref(false)
const userTravels: Ref<Travel[]> = ref([])
const userFines: Ref<Fine[]> = ref([])
const userCaCs: Ref<CaC[]> = ref([])
const activeTab = ref<'travels' | 'fines' | 'cacs'>('travels')

onMounted(() => {
  userStore.fetchUsers()
})

const filteredUsers = computed(() => {
  if (!searchQuery.value) {
    return userStore.users
  }
  const query = searchQuery.value.toLowerCase()
  return userStore.users.filter(user => {
    const name = (user.userName ?? '').toLowerCase()
    const mail = (user.email ?? '').toLowerCase()
    return name.includes(query) || mail.includes(query)
  })
})

const selectUser = async (user: User) => {
  selectedUser.value = user
  if (!user.uidUser) {
    console.error('No se encontró ID de usuario válido en el objeto seleccionado:', user)
    return
  }
  activeTab.value = 'travels'
  loadingDetails.value = true
  await Promise.all([
    userStore.fetchTravels(user.uidUser),
    userStore.fetchFines(user.uidUser),
    userStore.fetchCaCs(user.uidUser)
  ])
  userTravels.value = userStore.travels
  userFines.value = userStore.fines
  userCaCs.value = userStore.cacs
  loadingDetails.value = false
}

const closeModal = () => {
  selectedUser.value = null
  userTravels.value = []
  userFines.value = []
  userCaCs.value = []
}

const formatDate = (date: Date) => {
  return new Date(date).toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getSubscriptionClass = (subscription: unknown) => {
  const sub = typeof subscription === 'string' ? subscription.toLowerCase() : 'none'
  if (!sub || sub === 'none' || sub === 'null' || sub === 'undefined') return 'none'

  return 'basic'
}
</script>

<style scoped>
.user-list-container {
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  margin-bottom: 2rem;
}

.header h1 {
  margin: 0 0 1rem 0;
  color: var(--color-text-primary-light);
  font-size: 2rem;
}

.search-box {
  display: flex;
  gap: 1rem;
}

.search-input {
  flex: 1;
  max-width: 400px;
  padding: 0.75rem 1rem;
  border: 2px solid var(--color-border-light);
  border-radius: var(--border-radius);
  font-size: 1rem;
  transition: border-color 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: var(--color-green-main);
  box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.1);
}

.table-container {
  background: var(--color-background-light);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table thead {
  background: var(--color-primary-light);
  color: var(--color-white);
}

.users-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.875rem;
  letter-spacing: 0.5px;
}

.users-table tbody tr {
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.user-row {
  cursor: pointer;
}

.user-row:hover {
  background-color: var(--color-gray-very-light);
  transform: scale(1.01);
}

.user-row.selected {
  background-color: rgba(46, 125, 50, 0.1);
}

.users-table td {
  padding: 1rem;
  color: var(--color-text-secondary-light);
}

.badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
}

.badge-none {
  background-color: var(--color-gray-light);
  color: var(--color-gray-dark);
}

.badge-basic {
  background-color: var(--color-blue-light);
  color: var(--color-white);
}

.badge-premium {
  background: var(--color-accent-light);
  color: var(--color-gray-dark);
}

.btn-view {
  padding: 0.5rem 1rem;
  background: var(--color-primary-light);
  color: var(--color-white);
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-view:hover {
  background: var(--color-green-light);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.no-data {
  padding: 3rem;
  text-align: center;
  color: #999;
  font-size: 1.125rem;
}

@media (max-width: 768px) {
  .users-table {
    font-size: 0.875rem;
  }

  .users-table th,
  .users-table td {
    padding: 0.75rem 0.5rem;
  }
}
</style>
