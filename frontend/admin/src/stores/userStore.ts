import { defineStore } from 'pinia';
import type User from '@/models/User';
import type Travel from '@/models/Travel';
import type Fine from '@/models/Fine';
import type CaC from '@/models/CaC';

const usersStore = defineStore('users', {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL,
    users: [] as User[],
    travels: [] as Travel[],
    fines: [] as Fine[],
    cacs: [] as CaC[],
  }),
  actions: {
    async fetchUsers() {
      try {
        const response = await fetch(`${this.baseURL}/user/`, {
          headers: { Accept: 'application/json' }
        });
        if (!response.ok) {
          console.error('HTTP error fetching users:', response.status, response.statusText)
          this.users = []
          return
        }
        const data = await response.json()
        this.users = Array.isArray(data) ? data as User[] : []
      }catch (error) {
        console.error('Error fetching users:', error);
        this.users = []
      }
    },
    async fetchTravels(id: string) {
      try {
        const response = await fetch(`${this.baseURL}/travel/usuario/${id}`, {
          headers: { Accept: 'application/json' }
        })
        if (!response.ok) {
          console.error('HTTP error fetching travels:', response.status, response.statusText)
          this.travels = []
          return
        }
        const data = await response.json()
        this.travels = Array.isArray(data) ? data as Travel[] : []
      } catch (error) {
        console.error('Error fetching travels:', error)
        this.travels = []
      }

    },
    async fetchAllTravels() {
      try {
        const response = await fetch(`${this.baseURL}/travel/`, {
          headers: { Accept: 'application/json' }
        })
        if (!response.ok) {
          console.error('HTTP error fetching all travels:', response.status, response.statusText)
          this.travels = []
          return
        }
        const data = await response.json()
        this.travels = Array.isArray(data) ? data as Travel[] : []
      } catch (error) {
        console.error('Error fetching all travels:', error)
        this.travels = []
      }
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
    async fetchAllFines() {
      try {
        // Intentar primero obtener todas las multas de todos los usuarios
        const usersResponse = await fetch(`${this.baseURL}/user/`, {
          headers: { Accept: 'application/json' }
        })

        if (!usersResponse.ok) {
          console.error('Error obteniendo usuarios para multas')
          this.fines = []
          return
        }

        const users = await usersResponse.json()
        const allFines: Fine[] = []

        // Obtener multas de cada usuario
        for (const user of users) {
          try {
            const userId = user.uidUser || user.idUser || user.id || user.userId
            if (!userId) continue

            const finesResponse = await fetch(`${this.baseURL}/fine/api/user_fines/user/${userId}`, {
              headers: { Accept: 'application/json' }
            })

            if (finesResponse.ok) {
              const userFines = await finesResponse.json()
              if (Array.isArray(userFines)) {
                allFines.push(...userFines)
              }
            }
          } catch (error) {
            console.log(`Error obteniendo multas del usuario:`, error)
          }
        }

        console.log('Multas recibidas:', allFines)
        this.fines = allFines
        console.log('Multas en store:', this.fines)
      } catch (error) {
        console.error('Error fetching all fines:', error)
        this.fines = []
      }
    },
    async fetchCaCs(id: string) {
      try {
        const response = await fetch(`${this.baseURL}/cac/user/${id}`, {
          headers: { Accept: 'application/json' }
        })
        if (!response.ok) {
          console.error('HTTP error fetching CaCs:', response.status, response.statusText)
          this.cacs = []
          return
        }
        const data = await response.json()
        this.cacs = Array.isArray(data) ? data as CaC[] : []
      } catch (error) {
        console.error('Error fetching CaCs:', error)
        this.cacs = []
      }
    }

  }
});

export default usersStore;
