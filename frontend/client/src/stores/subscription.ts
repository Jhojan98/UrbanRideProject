import { defineStore } from 'pinia'

interface SubscriptionResponse {
  uidUser: string
  userEmail: string
  userName: string
  subscriptionType: string
  balance: number
  subscriptionTravels: number
}

interface SubscriptionRequest {
  userId: string
  token?: string
}

const useSubscriptionStore = defineStore('subscription', {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL || 'http://localhost:8090',
    loading: false,
    error: null as string | null,
    subscriptionData: null as SubscriptionResponse | null,
    userBalance: 0 as number
  }),

  actions: {
    /**
     * Obtener balance del usuario para validar suscripción
     */
    async fetchUserBalance(userId: string): Promise<number | null> {
      try {
        const response = await fetch(`${this.baseURL}/user/balance/${userId}`, {
          method: 'GET',
          headers: {
            'Accept': 'application/json',
          },
        })

        if (!response.ok) {
          console.error('Error obteniendo balance:', response.status)
          return null
        }

        const data = await response.json()
        this.userBalance = data.balance || data.saldo || 0
        return this.userBalance
      } catch (error) {
        console.error('Error en fetchUserBalance:', error)
        return null
      }
    },

    /**
     * Comprar suscripción mensual
     */
    async purchaseMonthlySubscription(
      request: SubscriptionRequest
    ): Promise<SubscriptionResponse | null> {
      const { userId, token } = request

      if (!userId) {
        this.error = 'ID de usuario requerido'
        return null
      }

      // Primero validamos que tenga saldo suficiente (>= 150,000 COP)
      const currentBalance = await this.fetchUserBalance(userId)
      
      if (currentBalance === null) {
        this.error = 'No se pudo verificar el saldo del usuario'
        return null
      }

      if (currentBalance < 150000) {
        this.error = 'Saldo insuficiente. Se requieren mínimo $150,000 COP para la suscripción.'
        return null
      }

      this.loading = true
      this.error = null

      try {
        const headers: Record<string, string> = {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        }

        if (token) {
          headers['Authorization'] = `Bearer ${token}`
        }

        const response = await fetch(
          `${this.baseURL}/user/subscription/purchase/${userId}`,
          {
            method: 'POST',
            headers,
          }
        )

        if (!response.ok) {
          let errBody: any = null
          try {
            errBody = await response.json()
          } catch (e) {
            /* no JSON */
          }

          const errMsg = errBody?.error || errBody?.mensaje || `Error del servidor: ${response.status}`
          this.error = errMsg
          console.error('HTTP error al comprar suscripción:', response.status, errBody)
          return null
        }

        const data = await response.json()
        
        if (!data) {
          this.error = 'Respuesta inválida del servidor'
          return null
        }

        this.subscriptionData = data as SubscriptionResponse
        // Actualizar balance después de la compra
        this.userBalance = data.balance || 0
        
        return this.subscriptionData
      } catch (error: any) {
        this.error = `Error al procesar suscripción: ${error?.message || 'Error desconocido'}`
        console.error('Error en purchaseMonthlySubscription:', error)
        return null
      } finally {
        this.loading = false
      }
    },

    /**
     * Verificar si usuario puede suscribirse (balance >= 150,000)
     */
    async canSubscribe(userId: string): Promise<boolean> {
      const balance = await this.fetchUserBalance(userId)
      return balance !== null && balance >= 150000
    },

    /**
     * Limpiar errores
     */
    clearError(): void {
      this.error = null
    },

    /**
     * Resetear estado
     */
    reset(): void {
      this.subscriptionData = null
      this.error = null
      this.loading = false
    }
  },

  getters: {
    /**
     * Formatear balance para mostrar
     */
    formattedBalance: (state) => {
      return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0
      }).format(state.userBalance)
    },

    /**
     * Verificar si usuario ya tiene suscripción activa
     */
    hasActiveSubscription: (state) => {
      return state.subscriptionData?.subscriptionType === 'MONTHLY'
    }
  }
})

export default useSubscriptionStore