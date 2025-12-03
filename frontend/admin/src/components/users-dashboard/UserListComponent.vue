<template>
  <div class="user-list-container">
    <div class="header">
      <h1>Lista de Usuarios</h1>
      <div class="search-box">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Buscar usuario por nombre o email..."
          class="search-input"
        />
      </div>
    </div>

    <div class="table-container">
      <table class="users-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Usuario</th>
            <th>Email</th>
            <th>Suscripción</th>
            <th>Fecha de Registro</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="user in filteredUsers"
            :key="user.idUser"
            @click="selectUser(user)"
            :class="{ 'selected': selectedUser?.idUser === user.idUser }"
            class="user-row"
          >
            <td>{{ user.idUser }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.email }}</td>
            <td>
              <span :class="['badge', `badge-${getSubscriptionClass(user.subscription)}`]">
                {{ user.subscription }}
              </span>
            </td>
            <td>{{ formatDate(user.timestamp) }}</td>
            <td>
              <button
                @click.stop="selectUser(user)"
                class="btn-view"
              >
                Ver Historial
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredUsers.length === 0" class="no-data">
        <p>No se encontraron usuarios</p>
      </div>
    </div>

    <!-- Modal de historial de viajes -->
    <div v-if="selectedUser" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Historial de Viajes - {{ selectedUser.username }}</h2>
          <button @click="closeModal" class="btn-close">✕</button>
        </div>

        <div class="modal-body">
          <div v-if="loadingTravels" class="loading">
            Cargando viajes...
          </div>

          <div v-else-if="userTravels.length === 0" class="no-data">
            <p>Este usuario no tiene viajes registrados</p>
          </div>

          <div v-else class="travels-list">
            <div
              v-for="travel in userTravels"
              :key="travel.idTravel"
              class="travel-card"
            >
              <div class="travel-header">
                <span class="travel-id">Viaje #{{ travel.idTravel }}</span>
                <span :class="['status-badge', `status-${travel.status.toLowerCase()}`]">
                  {{ travel.status }}
                </span>
              </div>

              <div class="travel-details">
                <div class="detail-row">
                  <span class="label">Bicicleta:</span>
                  <span class="value">{{ travel.idBicycle }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">Estación de Inicio:</span>
                  <span class="value">{{ travel.startStation }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">Estación de Fin:</span>
                  <span class="value">{{ travel.endStation || 'En curso' }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">Inicio:</span>
                  <span class="value">{{ formatDateTime(travel.startTimestamp) }}</span>
                </div>
                <div class="detail-row" v-if="travel.endTimestamp">
                  <span class="label">Fin:</span>
                  <span class="value">{{ formatDateTime(travel.endTimestamp) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import type { Ref } from 'vue'
import usersStore from '@/stores/usersStore'
import type User from '@/models/User'
import type Travel from '@/models/Travel'

const userStore = usersStore()
const selectedUser: Ref<User | null> = ref(null)
const searchQuery = ref('')
const loadingTravels = ref(false)
const userTravels: Ref<Travel[]> = ref([])

onMounted(() => {
  userStore.fetchUsers()
})

const filteredUsers = computed(() => {
  if (!searchQuery.value) {
    return userStore.users
  }
  const query = searchQuery.value.toLowerCase()
  return userStore.users.filter(user =>
    user.username.toLowerCase().includes(query) ||
    user.email.toLowerCase().includes(query)
  )
})

const selectUser = async (user: User) => {
  selectedUser.value = user
  loadingTravels.value = true
  await userStore.fetchTravels(user.idUser)
  userTravels.value = userStore.travels
  loadingTravels.value = false
}

const closeModal = () => {
  selectedUser.value = null
  userTravels.value = []
}

const formatDate = (date: Date) => {
  return new Date(date).toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatDateTime = (date: Date) => {
  return new Date(date).toLocaleString('es-ES', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getSubscriptionClass = (subscription: string) => {
  const sub = subscription.toLowerCase()
  if (sub === 'none') return 'none'
  if (sub.includes('premium') || sub.includes('gold')) return 'premium'
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

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: var(--color-background-light);
  border-radius: 16px;
  width: 90%;
  max-width: 800px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid var(--color-border-light);
  background: var(--color-primary-light);
  color: var(--color-white);
  border-radius: 16px 16px 0 0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.btn-close {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.btn-close:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.modal-body {
  padding: 2rem;
  overflow-y: auto;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
  font-size: 1.125rem;
}

.travels-list {
  display: grid;
  gap: 1rem;
}

.travel-card {
  background: var(--color-gray-very-light);
  border-radius: 12px;
  padding: 1.5rem;
  border-left: 4px solid var(--color-gray-medium);
  transition: all 0.3s ease;
}

.travel-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateX(4px);
}

.travel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #e0e0e0;
}

.travel-id {
  font-weight: 700;
  color: var(--color-text-primary-light);
  font-size: 1.125rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-completed {
  background-color: #d4edda;
  color: #155724;
}

.status-active,
.status-en_curso {
  background-color: #fff3cd;
  color: #856404;
}

.status-cancelled,
.status-cancelado {
  background-color: #f8d7da;
  color: #721c24;
}

.travel-details {
  display: grid;
  gap: 0.75rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-row .label {
  font-weight: 600;
  color: var(--color-gray-medium);
}

.detail-row .value {
  color: var(--color-text-primary-light);
  font-weight: 500;
}

@media (max-width: 768px) {
  .users-table {
    font-size: 0.875rem;
  }

  .users-table th,
  .users-table td {
    padding: 0.75rem 0.5rem;
  }

  .modal-content {
    width: 95%;
    max-height: 90vh;
  }

  .modal-header {
    padding: 1rem;
  }

  .modal-body {
    padding: 1rem;
  }
}
</style>
