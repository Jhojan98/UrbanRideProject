import { defineStore } from 'pinia'
import { getAuth, signInWithEmailAndPassword, signOut } from "firebase/auth";

const userAuthStore = defineStore('auth', {
    state() {
        return {
            token: null as string | null,
            baseURL:''
        }
    },
    actions: {

        async login(email: string, password: string) {

            try {
                const auth = getAuth()

                const userCredential = await signInWithEmailAndPassword(auth, email, password);
                const user = userCredential.user;
                this.token = await user.getIdToken();
            } catch (error) {
                console.error("Error during login:", error);
                throw error;
            }

        }
    }
})
export default userAuthStore