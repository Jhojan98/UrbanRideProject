import { defineStore } from 'pinia'

const useAuthStore = defineStore('auth', {
    state: () => ({
        token: null as string | null,
        user: '',
        message: '',
        baseURL: ''
    }),
    getters: {
        isAuthenticated: (state): boolean => !!state.token,
        getUser: (state): string => state.user
    },
    actions: {
        async login(email: string, _password: string) {
            // TODO: Implementar lógica de login con API
            console.log('Login attempt:', email)
            // Simulación de login exitoso
            this.token = 'mock-token-' + Date.now()
            this.user = email
            this.message = 'Login successful'
            
            // Guardar en localStorage para persistencia
            localStorage.setItem('auth_token', this.token)
            localStorage.setItem('auth_user', this.user)
            
            return true
        },
        logout() {
            this.token = null
            this.user = ''
            this.message = ''
            
            // Limpiar localStorage
            localStorage.removeItem('auth_token')
            localStorage.removeItem('auth_user')
        },
        // Método para restaurar la sesión desde localStorage
        restoreSession() {
            const token = localStorage.getItem('auth_token')
            const user = localStorage.getItem('auth_user')
            
            if (token && user) {
                this.token = token
                this.user = user
            }
        }
    }
})

export default useAuthStore