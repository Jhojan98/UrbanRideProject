import { defineStore } from 'pinia';
import User from '@/models/User';
import Travel from '@/models/Travel';
import Fine from '@/models/Fine';
import CaC from '@/models/CaC';

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
        const response = await fetch(`${this.baseURL}/admin/`);
      }catch (error) {
        console.error('Error fetching users:', error);
      }
    },
    async fetchTravels(id:number) {
      console.log('Fetching travels by user id');

    },
    async fetchFines(id: number) {
      console.log('Fetching fines by user id');
    },
    async fetchCaCs(id: number) {
      console.log('Fetching CaCs by user id');
    }

  }
});

export default usersStore;
