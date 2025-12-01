import { defineStore } from 'pinia'
import { getAuth, signInWithEmailAndPassword, signOut } from "firebase/auth";

const userAuthStore = defineStore('auth', {
    state() {
        return {
            token: null as string | null,
            baseURL: process.env.VUE_APP_API_URL || 'http://localhost:8090',
            message: '',
            isVerified: false,
            pendingVerification: false,
            tempEmail: null as string | null
        }
    },
    actions: {

        async login(email: string, password: string): Promise<{ success?: boolean; userData?: unknown }> {
            try {
                const auth = getAuth();

                // Iniciar sesión con Firebase
                const userCredential = await signInWithEmailAndPassword(auth, email, password);

                // Obtener token de Firebase
                const token = await userCredential.user.getIdToken();

                console.log('=== ENVIANDO DATOS AL BACKEND (ADMIN LOGIN) ===');
                console.log('email:', email);
                console.log('uid:', userCredential.user.uid);
                console.log('token:', token ? 'Token obtenido' : 'No token');
                console.log('================================================');

                // Verificar si el administrador existe en la base de datos
                const uri = `${this.baseURL}/admin/login/${userCredential.user.uid}`;
                const rawResponse = await fetch(uri, {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json',
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (!rawResponse.ok) {
                    if (rawResponse.status === 404) {
                        console.error('Administrador no encontrado en la base de datos');
                        this.message = 'No tienes permisos de administrador. Contacta al soporte.';
                    } else {
                        console.error('Error HTTP del backend (login):', rawResponse.status, rawResponse.statusText);
                        this.message = `Error al validar con backend: ${rawResponse.statusText}`;
                    }
                    return { success: false };
                }

                const response = await rawResponse.json();
                console.log('Administrador verificado:', response);

                this.token = token;
                this.isVerified = true;
                this.pendingVerification = false;
                this.message = 'Login exitoso';

                return { success: true, userData: response };
            } catch (error: unknown) {
                console.error('Error en login:', error);

                const firebaseError = error as { code?: string };

                console.error('Código de error:', firebaseError.code);

                this.token = null;
                return { success: false };
            }
        },
        async logout() {
            try {
                const auth = getAuth();
                await signOut(auth);
                this.token = null;
                this.isVerified = false;
                this.message = 'Sesión cerrada';
            } catch (error: unknown) {
                console.error('Error en logout:', error);
                this.message = 'Error al cerrar sesión';
            }
        },
    }
})
export default userAuthStore