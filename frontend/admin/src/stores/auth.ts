import { defineStore } from "pinia";
import {
  createUserWithEmailAndPassword,
  getAuth,
  signInWithEmailAndPassword,
  signOut,
} from "firebase/auth";

const INACTIVITY_TIMEOUT = 10 * 60 * 1000; // 10 minutos en milisegundos

export const useAuthStore = defineStore("auth", {
  state() {
    // Verificar si el token ha expirado por inactividad
    const lastActivity = localStorage.getItem('lastActivity');
    const now = Date.now();
    let token = localStorage.getItem('adminToken') as string | null;
    let isVerified = localStorage.getItem('isVerified') === 'true';

    if (lastActivity && token) {
      const timeSinceLastActivity = now - parseInt(lastActivity);
      if (timeSinceLastActivity > INACTIVITY_TIMEOUT) {
        // Token expirado, limpiar todo
        localStorage.removeItem('adminToken');
        localStorage.removeItem('isVerified');
        localStorage.removeItem('lastActivity');
        token = null;
        isVerified = false;
      }
    }

    return {
      token,
      baseURL: "/api",
      message: "",
      isVerified,
      pendingVerification: false,
      tempEmail: null as string | null,
    };
  },
  actions: {
    async login(
      email: string,
      password: string
    ): Promise<{ success?: boolean; userData?: unknown }> {
      try {
        const auth = getAuth();

        // Iniciar sesión con Firebase
        const userCredential = await signInWithEmailAndPassword(
          auth,
          email,
          password
        );

        // Obtener token de Firebase
        const token = await userCredential.user.getIdToken();

        // Verificar si el administrador existe en la base de datos
        const uri = `${this.baseURL}/admin/login/${userCredential.user.uid}`;
        const rawResponse = await fetch(uri, {
          method: "GET",
          headers: {
            Accept: "application/json",
            Authorization: `Bearer ${token}`,
          },
        });

        if (!rawResponse.ok) {
          if (rawResponse.status === 404) {
            console.error("Administrador no encontrado en la base de datos");
            this.message =
              "No tienes permisos de administrador. Contacta al soporte.";
          } else {
            console.error(
              "Error HTTP del backend (login):",
              rawResponse.status,
              rawResponse.statusText
            );
            this.message = `Error al validar con backend: ${rawResponse.statusText}`;
          }
          return { success: false };
        }

        const response = await rawResponse.json();
        console.log("Administrador verificado:", response);

        this.token = token;
        this.isVerified = true;
        this.pendingVerification = false;
        this.message = "Login exitoso";

        // Guardar token en localStorage para persistencia
        localStorage.setItem('adminToken', token);
        localStorage.setItem('isVerified', 'true');
        localStorage.setItem('lastActivity', Date.now().toString());

        return { success: true, userData: response };
      } catch (error: unknown) {
        console.error("Error en login:", error);

        const firebaseError = error as { code?: string; message?: string };

        console.error("Código de error:", firebaseError.code);

        // Mapear errores de Firebase a mensajes amigables
        if (firebaseError.code === 'auth/user-not-found') {
          this.message = 'Usuario no encontrado';
        } else if (firebaseError.code === 'auth/wrong-password') {
          this.message = 'Contraseña incorrecta';
        } else if (firebaseError.code === 'auth/invalid-email') {
          this.message = 'Email inválido';
        } else if (firebaseError.code === 'auth/too-many-requests') {
          this.message = 'Demasiados intentos. Intenta más tarde';
        } else {
          this.message = firebaseError.message || 'Error al iniciar sesión';
        }

        this.token = null;
        this.isVerified = false;
        return { success: false };
      }
    },
    async createAdminAccount(email: string, password: string,username:string) {
      try {
        const auth = getAuth();
        const newAdmin = await createUserWithEmailAndPassword(
          auth,
          email,
          password
        );

        const uri = `${this.baseURL}/admin/register`;
        const rawResponse = await fetch(uri, {
          method: "POST",
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            uidUser: newAdmin.user.uid,
            userName: username,
          }),
        });
        if (!rawResponse.ok) {
          console.error(
            "Error HTTP del backend:",
            rawResponse.status,
            rawResponse.statusText
          );
          const errorText = await rawResponse.text();
          console.error("Respuesta del servidor:", errorText);
          this.message = `Error al guardar en backend: ${rawResponse.statusText}`;
          return false;
        }

        const response = await rawResponse.json();
        console.log("Respuesta del backend (register):", response);

        return true;
      } catch (error) {
        console.error("Error creating admin account:", error);
        return false;
      }
    },
    renewActivity() {
      // Renovar timestamp de última actividad
      if (this.token) {
        localStorage.setItem('lastActivity', Date.now().toString());
      }
    },
    checkTokenExpiration(): boolean {
      // Verificar si el token ha expirado
      const lastActivity = localStorage.getItem('lastActivity');
      if (!lastActivity || !this.token) {
        return false;
      }

      const timeSinceLastActivity = Date.now() - parseInt(lastActivity);
      if (timeSinceLastActivity > INACTIVITY_TIMEOUT) {
        // Token expirado, limpiar todo
        this.logout();
        this.message = 'Sesión expirada por inactividad';
        return true;
      }
      return false;
    },
    async logout() {
      try {
        const auth = getAuth();
        await signOut(auth);
        this.token = null;
        this.isVerified = false;
        this.message = "Sesión cerrada";

        // Limpiar localStorage
        localStorage.removeItem('adminToken');
        localStorage.removeItem('isVerified');
        localStorage.removeItem('lastActivity');
      } catch (error: unknown) {
        console.error("Error en logout:", error);
        this.message = "Error al cerrar sesión";
      }
    },
  },
});
