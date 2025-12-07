import { defineStore } from 'pinia'
import type Fine from '@/models/Fine'
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
        fines: [] as Fine[],
    }
  },

  actions: {
    /**
     * Get user balance through the gateway
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
          console.error('Error getting balance:', response.status, response.statusText)
          return null
        }

        const data = await response.json()
        return data.balance || data.saldo || 0
      } catch (error) {
        console.error('Error in fetchBalance:', error)
        return null
      }
    },

    /**
     * Create checkout session in Stripe
     */
    async createCheckoutSession(
      request: CheckoutSessionRequest,
      token?: string
    ): Promise<CheckoutSessionResponse | null> {
      if (!request.priceId || !request.userId) {
        this.error = 'Incomplete required data'
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

          const errMsg = errBody?.error || `Server error: ${response.status}`
          this.error = errMsg
          console.error('HTTP error from backend:', response.status, response.statusText, errBody)
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
     * Add balance to user
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
          this.error = errData.error || `Error adding balance: ${response.status}`
          console.error('Error in addBalance:', this.error)
          return null
        }

        const data = await response.json()
        return data.balance || data.saldo || null
      } catch (error: any) {
        this.error = `Error adding balance: ${error?.message || 'Unknown error'}`
        console.error('Error in addBalance:', error)
        return null
      }
    },

    /**
     * Subtract balance from user
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
        async fetchFines(id: string) {
      try {
        const response = await fetch(`${this.baseURL}/fine/api/user_fines/user/${id}`, {
          headers: { Accept: 'application/json' }
        })
        if (!response.ok) {
          console.error('HTTP error fetching fines:', response.status, response.statusText)
          this.fines = []
          return
        }
        const data = await response.json()
        this.fines = Array.isArray(data) ? data as Fine[] : []
      } catch (error) {
        console.error('Error fetching fines:', error)
        this.fines = []
      }
    },
    async payFine(idUserFine: number, userId: string, amount: number): Promise<boolean> {
      try {
        const url = `${this.baseURL}/fine/api/user_fines/${idUserFine}/pay`;
        const response = await fetch(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
          },
        })
        if (!response.ok) {
          const errData = await response.json().catch(() => ({}))
          this.error = errData.error || `Error paying fine: ${response.status}`
          console.error('Error paying fine:', this.error)
          return false
        }

        // Update user balance by subtracting the fine amount
        const newBalance = await this.fetchBalance(userId);
        if (newBalance === null) {
          this.error = 'Error updating balance after paying the fine';
          return false;
        }

        // Refresh the list of fines
        await this.fetchFines(userId);
        return true;
      } catch (error) {
        console.error('Error paying fine:', error);
        this.error = 'Error connecting to server to pay the fine';
        return false;
      }
    }
  },
})

export default usePaymentStore
