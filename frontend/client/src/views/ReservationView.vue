<template>
  <div class="reservation-view">
    <template v-if="!hasActiveReservation">
      <div class="reservation-grid">
        <aside class="left-panel">
          <ReserveFormComponent
            :station="origin"
            :balance="userBalance"
            :origin="origin"
            :destination="destination"
            @update:origin="(o) => origin = o"
            @update:destination="(d) => destination = d"
          />
        </aside>
        <section class="map-panel">
          <MapComponent :origin="origin" :destination="destination" :use-sockets="false" />
        </section>
      </div>
    </template>
    <ConfirmationComponent v-else />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ConfirmationComponent from '@/components/reservation/ConfirmationComponent.vue';
import MapComponent from '@/components/reservation/MapComponent.vue';
import ReserveFormComponent from '@/components/reservation/ReserveFormComponent.vue';
import { useReservation } from '@/composables/useReservation';
import usePaymentStore from '@/stores/payment';
import { getAuth } from 'firebase/auth';

const { hasActiveReservation } = useReservation();
const paymentStore = usePaymentStore();

// Tipos para coordinar origen/destino entre el formulario y el mapa
interface Endpoint {
  idStation?: number
  latitude: number
  longitude: number
  name?: string
  nameStation?: string
  free_spots?: number
  status?: string
  availableSlots?: number
  totalSlots?: number
  type?: string
  mechanical?: number
  electric?: number
}

const origin = ref<Endpoint | null>(null)
const destination = ref<Endpoint | null>(null)
const userBalance = ref<number>(0)
const isLoadingBalance = ref<boolean>(false)

// Cargar el balance real del usuario
async function fetchUserBalance() {
  try {
    isLoadingBalance.value = true
    const firebaseAuth = getAuth()
    const currentUser = firebaseAuth.currentUser
    
    if (currentUser?.uid) {
      const balance = await paymentStore.fetchBalance(currentUser.uid)
      userBalance.value = balance ?? 0
      console.log('[ReservationView] Balance cargado:', userBalance.value)
    }
  } catch (error) {
    console.error('[ReservationView] Error cargando balance:', error)
    userBalance.value = 0
  } finally {
    isLoadingBalance.value = false
  }
}

onMounted(() => {
  fetchUserBalance()
})

// Selection by map click disabled; origin/destination are chosen via dropdown in the form.
</script>


<style lang="scss" scoped>
  @import "@/styles/maps.scss";
</style>
