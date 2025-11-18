import { ref, computed } from 'vue'

interface ReservationData {
  bikeType: string
  rideType: string
  station?: {
    name: string
    available: number
    status: string
  }
}

const reservationData = ref<ReservationData | null>(null)
const isReservationActive = ref(false)

export function useReservation() {
  const setReservation = (data: ReservationData) => {
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
