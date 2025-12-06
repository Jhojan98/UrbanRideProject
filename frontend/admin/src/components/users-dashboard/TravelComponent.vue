<template>
    <div class="travel-table-container">
        <div class="header">
            <h2>Viajes</h2>
            <div class="actions">
                <button class="refresh" @click="loadTravels" :disabled="loading">
                    {{ loading ? 'Cargando...' : 'Actualizar' }}
                </button>
            </div>
        </div>

        <div class="table-wrapper" v-if="travels.length > 0">
            <table class="travel-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Estación Inicio</th>
                        <th>Estación Fin</th>
                        <th>Inicio</th>
                        <th>Fin</th>
                        <th>Estado</th>
                        <th>Tipo</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="t in travels" :key="t.idTravel">
                        <td>#{{ t.idTravel }}</td>
                        <td>{{ t.fromIdStation }}</td>
                        <td>{{ t.toIdStation ?? '' }}</td>
                        <td>{{ t.startedAt ? formatDateTime(t.startedAt) : '' }}</td>
                        <td>{{ t.endedAt ? formatDateTime(t.endedAt) : '' }}</td>
                        <td>
                            <span :class="['status-badge', `status-${(t.status ?? '').toLowerCase()}`]">
                                {{ t.status }}
                            </span>
                        </td>
                        <td>{{ t.travelType }}</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div v-else class="no-data">No hay viajes registrados.</div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import usersStore from '@/stores/userStore'
import type Travel from '@/models/Travel'

const store = usersStore()
const travels = ref<Travel[]>([])
const loading = ref(false)

const formatDateTime = (date: Date | string) => {
    const d = new Date(date)
    return d.toLocaleString('es-ES', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    })
}

const loadTravels = async () => {
    loading.value = true
    await store.fetchAllTravels()
    travels.value = store.travels
    loading.value = false
}

onMounted(loadTravels)
</script>

<style scoped>
.travel-table-container {
    background: var(--color-background-light);
    border-radius: 12px;
    padding: 1rem;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.actions .refresh {
    padding: 0.5rem 0.9rem;
    border-radius: 8px;
    border: 1px solid var(--color-border-light);
    background: var(--color-background-light);
    color: var(--color-text-primary-light);
    cursor: pointer;
}

.table-wrapper {
    overflow-x: auto;
}

.travel-table {
    width: 100%;
    border-collapse: collapse;
}

.travel-table th,
.travel-table td {
    padding: 0.75rem;
    border-bottom: 1px solid var(--color-border-light);
    text-align: left;
}

.status-badge {
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
}

.status-completed { background-color: #d4edda; color: #155724; }
.status-active, .status-en_curso { background-color: #fff3cd; color: #856404; }
.status-cancelled, .status-cancelado { background-color: #f8d7da; color: #721c24; }

.no-data {
    padding: 2rem;
    text-align: center;
    color: #666;
}
</style>
