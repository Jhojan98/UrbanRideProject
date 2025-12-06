import { defineStore } from 'pinia'

interface CheckoutSessionRequest {
  priceId: string
  quantity: number
  customerEmail?: string
  userId: string
}

interface CheckoutSessionResponse {
  url: string
  sessionId?: string
}

const usePaymentStore = defineStore('payment', {
  state() {
    return {
      baseURL: process.env.VUE_APP_API_URL || 'http://localhost:8090',
      loading: false,
      error: null as string | null,
    }
  },

  actions: {
    /**
     * Obtener balance del usuario a través del gateway
     */
    async fetchBalance(userId: string): Promise<number | null> {
      if (!userId) return null

      try {
        const response = await fetch(`${this.baseURL}/user/balance/${userId}`, {
          method: 'GET',
          headers: {
            'Accept': 'application/json',
          },
        })

        if (!response.ok) {
          console.error('Error obteniendo balance:', response.status, response.statusText)
          return null
        }

        const data = await response.json()
        return data.balance || data.saldo || 0
      } catch (error) {
        console.error('Error en fetchBalance:', error)
        return null
      }
    },

    /**
     * Crear sesión de checkout en Stripe
     */
    async createCheckoutSession(
      request: CheckoutSessionRequest,
      token?: string
    ): Promise<CheckoutSessionResponse | null> {
      if (!request.priceId || !request.userId) {
        this.error = 'Datos requeridos incompletos'
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

        const response = await fetch(`${this.baseURL}/payment/payments/checkout-session`, {
          method: 'POST',
          headers,
          body: JSON.stringify(request),
        })

        if (!response.ok) {
          let errBody: any = null
          try {
            errBody = await response.json()
          } catch (e) {
            /* no JSON */
          }

          const errMsg = errBody?.error || `Error del servidor: ${response.status}`
          this.error = errMsg
          console.error('Error HTTP del backend:', response.status, response.statusText, errBody)
          return null
        }

        const data = await response.json()

        if (!data || !data.url) {
          this.error = 'Respuesta inválida del servidor al crear la sesión de pago'
          console.error('Respuesta inesperada del backend:', data)
          return null
        }

        return data as CheckoutSessionResponse
      } catch (error: any) {
        this.error = `Error al crear sesión de pago: ${error?.message || 'Error desconocido'}`
        console.error('Error en createCheckoutSession:', error)
        return null
      } finally {
        this.loading = false
      }
    },

    /**
     * Agregar saldo al usuario
     */
    async addBalance(userId: string, amount: number): Promise<number | null> {
      if (!userId || amount <= 0) return null

      try {
        const response = await fetch(`${this.baseURL}/user/balance/${userId}/add?amount=${amount}`, {
          method: 'POST',
          headers: {
            'Accept': 'application/json',
          },
        })

        if (!response.ok) {
          const errData = await response.json().catch(() => ({}))
          this.error = errData.error || `Error al agregar saldo: ${response.status}`
          console.error('Error en addBalance:', this.error)
          return null
        }

        const data = await response.json()
        return data.balance || data.saldo || null
      } catch (error: any) {
        this.error = `Error al agregar saldo: ${error?.message || 'Error desconocido'}`
        console.error('Error en addBalance:', error)
        return null
      }
    },

    /**
     * Restar saldo del usuario
     */
    async subtractBalance(userId: string, amount: number): Promise<number | null> {
      if (!userId || amount <= 0) return null

      try {
        const response = await fetch(`${this.baseURL}/user/balance/${userId}/subtract?amount=${amount}`, {
          method: 'POST',
          headers: {
            'Accept': 'application/json',
          },
        })

        if (!response.ok) {
          const errData = await response.json().catch(() => ({}))
          this.error = errData.error || `Error al restar saldo: ${response.status}`
          console.error('Error en subtractBalance:', this.error)
          return null
        }

        const data = await response.json()
        return data.balance || data.saldo || null
      } catch (error: any) {
        this.error = `Error al restar saldo: ${error?.message || 'Error desconocido'}`
        console.error('Error en subtractBalance:', error)
        return null
      }
    },

    /**
     * Marcar pago como completado en localStorage
     */
    markPaymentComplete(): void {
      localStorage.setItem('last_payment_time', Date.now().toString())
      localStorage.setItem('should_refresh_balance', 'true')
    },

    /**
     * Limpiar errores
     */
    clearError(): void {
      this.error = null
    },
  },
})

export default usePaymentStore
