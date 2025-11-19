import { defineStore } from 'pinia';

const useAuthStore = defineStore('auth', {
    state: () => ({
        token: null as string | null,
        user: "",
        message: "",
        baseURL:''
    }),
    actions: {
        async login(email: string, password: string) {
            console.log('Unimplemented')
        }
    }
});
export default useAuthStore