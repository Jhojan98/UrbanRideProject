import { ref, computed } from 'vue'
import type { Station } from '@/models/Station'

interface ReservationData {
  bikeType: string
  rideType: string
  station?: Partial<Station> | null
  destination?: Partial<Station> | null
  startResponse?: unknown
}

const reservationData = ref<ReservationData | null>(null)
const isReservationActive = ref(false)

export function useReservation() {
  const setReservation = (data: ReservationData) => {
    console.log('[useReservation] Guardando reserva:', data);
    console.log('[useReservation] Station guardada:', data.station);
    console.log('[useReservation] Station idStation:', data.station?.idStation);
    reservationData.value = data
    isReservationActive.value = true
  }

  const clearReservation = () => {
    reservationData.value = null
    isReservationActive.value = false
  }

  const hasActiveReservation = computed(() => isReservationActive.value)

  const getTripType = computed(() => {
    if (!reservationData.value) return 'N/A'
    return reservationData.value.rideType === 'short_trip'
      ? 'Última Milla'
      : 'Recorrido Largo'
  })

  const getEstimatedCost = computed(() => {
    if (!reservationData.value) return '$0'
    return reservationData.value.rideType === 'short_trip'
      ? '$17.500'
      : '$25.000'
  })

  const getBikeType = computed(() => {
    if (!reservationData.value) return 'N/A'
    return reservationData.value.bikeType === 'mechanical'
      ? 'Mecánica'
      : 'Eléctrica'
  })

  return {
    reservationData,
    isReservationActive,
    hasActiveReservation,
    setReservation,
    clearReservation,
    getTripType,
    getEstimatedCost,
    getBikeType
  }
}
