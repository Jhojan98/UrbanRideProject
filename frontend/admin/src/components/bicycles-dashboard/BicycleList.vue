<template>
    <div class="bicycle-list">
        <div class="list-header">
            <h3>Bicicletas Registradas</h3>
            <span class="bike-count">{{ bikes.length }} bicicletas</span>
        </div>

        <div v-if="bikes.length === 0" class="empty-state">
            No hay bicicletas registradas
        </div>

        <div v-else class="table-container">
            <table class="bikes-table">
                <thead>
                    <tr>
                        <th>Serie</th>
                        <th>ID</th>
                        <th>Tipo</th>
                        <th>Estado Bloqueo</th>
                        <th>Batería</th>
                        <th>Ubicación</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="bike in bikes" :key="bike.id" class="bike-row">
                        <td class="series">
                            <strong>#{{ bike.series }}</strong>
                        </td>
                        <td class="bike-id">
                            <code>{{ bike.id }}</code>
                        </td>
                        <td class="bike-type">
                            <span :class="['type-badge', bike.model.toLowerCase()]">
                                <span v-if="bike.model === 'ELECTRIC'">⚡</span>
                                <span v-else>🔧</span>
                                {{ bike.model === 'ELECTRIC' ? 'Eléctrica' : 'Mecánica' }}
                            </span>
                        </td>
                        <td class="lock-status">
                            <span :class="['status-badge', getLockStatusClass(bike.lockStatus)]">
                                {{ getLockStatusText(bike.lockStatus) }}
                            </span>
                        </td>
                        <td class="battery">
                            <span
                                v-if="bike.model === 'ELECTRIC'"
                                :class="['battery-indicator', getBatteryClass(bike.battery)]"
                            >
                                🔋 {{ bike.battery }}%
                            </span>
                            <span v-else class="na">N/A</span>
                        </td>
                        <td class="location">
                            <small v-if="bike.lat != null && bike.lon != null">
                                {{ bike.lat.toFixed(4) }}, {{ bike.lon.toFixed(4) }}
                            </small>
                            <span v-else class="na">Sin ubicación</span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type Bike from '@/models/Bike'

// eslint-disable-next-line no-undef
const props = defineProps<{ bikes: Bike[] }>()

const bikes = computed(() => props.bikes)

function getLockStatusClass(status: string): string {
    switch (status) {
        case 'UNLOCKED':
            return 'unlocked'
        case 'LOCKED':
            return 'locked'
        case 'ERROR':
            return 'error'
        default:
            return ''
    }
}

function getLockStatusText(status: string): string {
    switch (status) {
        case 'UNLOCKED':
            return 'Desbloqueada'
        case 'LOCKED':
            return 'Bloqueada'
        case 'ERROR':
            return 'Error'
        default:
            return status
    }
}

function getBatteryClass(battery: string): string {
    const level = parseInt(battery)
    if (level < 20) return 'low'
    if (level < 50) return 'medium'
    return 'high'
}
</script>

<style lang="scss" scoped>
.bicycle-list {
    background: var(--color-surface, white);
    border: 1px solid var(--color-border-light);
    border-radius: 8px;
    padding: 1.5rem;
}

.list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.list-header h3 {
    margin: 0;
}

.bike-count {
    color: var(--color-text-secondary-light);
    font-size: 0.9rem;
}

.empty-state {
    text-align: center;
    padding: 3rem 1rem;
    color: var(--color-text-secondary-light);
}

.table-container {
    overflow-x: auto;
    max-height: 600px;
    overflow-y: auto;
}

.bikes-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.9rem;
}

.bikes-table thead {
    position: sticky;
    top: 0;
    background: var(--color-surface, white);
    z-index: 1;
}

.bikes-table th {
    text-align: left;
    padding: 0.75rem 0.5rem;
    border-bottom: 2px solid var(--color-border-light);
    font-weight: 600;
    color: var(--color-text-primary);
}

.bikes-table td {
    padding: 0.75rem 0.5rem;
    border-bottom: 1px solid var(--color-border-light);
}

.bike-row:hover {
    background: var(--color-hover, #f8fafc);
}

.series strong {
    color: var(--color-primary);
}

.bike-id code {
    background: #f1f5f9;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 0.85rem;
    color: #64748b;
}

.type-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: 999px;
    font-size: 0.85rem;
    font-weight: 500;
}

.type-badge.electric {
    background: #dcfce7;
    color: #166534;
}

.type-badge.mechanic {
    background: #dbeafe;
    color: #1e40af;
}

.status-badge {
    display: inline-block;
    padding: 4px 10px;
    border-radius: 999px;
    font-size: 0.85rem;
    font-weight: 500;
}

.status-badge.unlocked {
    background: #dcfce7;
    color: #166534;
}

.status-badge.locked {
    background: #e5e7eb;
    color: #4b5563;
}

.status-badge.error {
    background: #fee2e2;
    color: #991b1b;
}

.battery-indicator {
    font-weight: 500;
}

.battery-indicator.high {
    color: #16a34a;
}

.battery-indicator.medium {
    color: #f59e0b;
}

.battery-indicator.low {
    color: #dc2626;
}

.na {
    color: var(--color-text-secondary-light);
    font-size: 0.85rem;
}

.location small {
    color: var(--color-text-secondary-light);
}
</style>
