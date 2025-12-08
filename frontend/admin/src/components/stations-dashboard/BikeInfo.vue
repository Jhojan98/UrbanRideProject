<template>
    <div class="bike-info" v-if="station">
        <div class="bike-info-header">
            <h3>{{ t('dashboard.bikes.title', { stationName: station.nameStation }) }}</h3>
            <p class="subtitle">{{ station.latitude }}, {{ station.longitude }}</p>
        </div>

        <div class="summary-grid">
            <div class="summary-card">
                <p>{{ t('dashboard.stations.summary.slots') }}</p>
                <strong>{{ station.availableSlots }} / {{ station.totalSlots }}</strong>
            </div>
            <div class="summary-card">
                <p>{{ t('dashboard.stations.summary.electric') }}</p>
                <strong>{{ station.availableElectricBikes ?? 0 }}</strong>
            </div>
            <div class="summary-card">
                <p>{{ t('dashboard.stations.summary.mechanic') }}</p>
                <strong>{{ station.availableMechanicBikes ?? 0 }}</strong>
            </div>
        </div>

        <div class="status-row">
            <span :class="['status-pill', station.cctvStatus ? 'on' : 'off']">
                {{ t('dashboard.stations.statusLabel.cctv') }}: {{ station.cctvStatus ? t('dashboard.stations.statusLabel.active') : t('dashboard.stations.statusLabel.inactive') }}
            </span>
            <span :class="['status-pill', station.lightingStatus ? 'on' : 'off']">
                {{ t('dashboard.stations.statusLabel.lighting') }}: {{ station.lightingStatus ? t('dashboard.stations.statusLabel.activeF') : t('dashboard.stations.statusLabel.inactiveF') }}
            </span>
            <span :class="['status-pill', station.panicButtonStatus ? 'on' : 'off']">
                {{ t('dashboard.stations.statusLabel.panic') }}: {{ station.panicButtonStatus ? t('dashboard.stations.statusLabel.active') : t('dashboard.stations.statusLabel.inactive') }}
            </span>
        </div>
    </div>
    <div v-else class="bike-info empty">{{ t('dashboard.isEmpty') }}</div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Station } from '@/models/Station'

const { t } = useI18n()
// eslint-disable-next-line no-undef
const props = defineProps<{ station: Station | null }>()
const station = computed(() => props.station)
</script>
<style lang="scss" scoped>
.bike-info { margin-top: 1rem; }
.bike-info-header { display: flex; align-items: baseline; gap: 1rem; }
.bike-info-header h3 { margin: 0; }
.subtitle { margin: 0; font-size: .85rem; color: var(--color-text-secondary-light); }

.summary-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
    gap: 0.75rem;
    margin: 1rem 0;
}

.summary-card {
    background: var(--color-surface, #f8fafc);
    border: 1px solid var(--color-border-light);
    border-radius: 8px;
    padding: 0.75rem;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.summary-card p { margin: 0; color: var(--color-text-secondary-light); font-size: 0.85rem; }
.summary-card strong { font-size: 1.1rem; }

.status-row {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.status-pill {
    padding: 6px 10px;
    border-radius: 999px;
    font-size: 0.85rem;
    border: 1px solid transparent;
}

.status-pill.on {
    background: #dcfce7;
    color: #166534;
    border-color: #86efac;
}

.status-pill.off {
    background: #fee2e2;
    color: #991b1b;
    border-color: #fecdd3;
}

.bike-info.empty {
    margin-top: 1rem;
}
</style>
