import { defineStore } from "pinia";
import { getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut, sendEmailVerification, GoogleAuthProvider, signInWithPopup} from 'firebase/auth';

const userAuth = defineStore("auth", {
    state() {
        return {
            token: null as string | null,
            baseURL: 'http://localhost:5001',
            message: '',
            isVerified: false,
            pendingVerification: false,
            tempEmail: null as string | null
        }
    },
    actions: {

        async register(id:number, username: string, password: string, fName: string, sName:string|"", fLastName: string, sLastName: string|"", birthDate: Date, email:string) {
            try {
                const auth = getAuth();

                const actionCodeSettings = {
                    url: 'http://localhost:8081/login',
                    handleCodeInApp: true
                }
                
                // Crear usuario en Firebase
                const userCredential = await createUserWithEmailAndPassword(auth, email, password);
                
                // Enviar email de verificación
                await sendEmailVerification(userCredential.user, actionCodeSettings);
                
                this.tempEmail = email;
                this.pendingVerification = true;
                this.isVerified = false;
                this.message = 'Registro exitoso. Revisa tu correo para verificar tu cuenta.';
                
                // TODO: Enviar datos al backend después de verificar email
                // const formattedDate = birthDate.toISOString().split('T')[0];
                // await fetch(`${this.baseURL}/auth/register`, { ... });
                
                return true;
            } catch (error: unknown) {
                console.error('Error en registro (detalle completo):', error);
                
                const firebaseError = error as { code?: string; message?: string };
                
                // Log detallado para depuración
                console.error('Código de error:', firebaseError.code);
                console.error('Mensaje de error:', firebaseError.message);
                
                switch (firebaseError.code) {
                    case 'auth/email-already-in-use':
                        this.message = 'El correo ya está registrado';
                        break;
                    case 'auth/invalid-email':
                        this.message = 'Correo inválido';
                        break;
                    case 'auth/weak-password':
                        this.message = 'La contraseña debe tener al menos 6 caracteres';
                        break;
                    case 'auth/operation-not-allowed':
                        this.message = 'Autenticación con email/contraseña no habilitada en Firebase';
                        break;
                    case 'auth/network-request-failed':
                        this.message = 'Error de red. Verifica tu conexión';
                        break;
                    default:
                        this.message = `Error en el registro: ${firebaseError.message || 'Error desconocido'}`;
                }
                return false;
            }
        },

        async login(email: string, password: string) { 
            try {
                const auth = getAuth();
                
                // Iniciar sesión con Firebase
                const userCredential = await signInWithEmailAndPassword(auth, email, password);
                
                // Verificar si el email está verificado
                if (!userCredential.user.emailVerified) {
                    this.tempEmail = email;
                    this.pendingVerification = true;
                    this.isVerified = false;
                    this.message = 'Por favor verifica tu correo antes de iniciar sesión';
                    
                    // Ofrecer reenvío de verificación
                    await sendEmailVerification(userCredential.user);
                    
                    return { needsVerification: true };
                }

                // Obtener token de Firebase
                const token = await userCredential.user.getIdToken();
                
                this.token = token;
                this.isVerified = true;
                this.pendingVerification = false;
                this.message = 'Login exitoso';
                
                // TODO: Enviar token al backend para validar y obtener datos del usuario
                // await fetch(`${this.baseURL}/auth/validate-token`, { ... });
                
                return { success: true };
            } catch (error: unknown) {
                console.error('Error en login:', error);
                
                const firebaseError = error as { code?: string };
                
                switch (firebaseError.code) {
                    case 'auth/user-not-found':
                    case 'auth/wrong-password':
                        this.message = 'Credenciales inválidas';
                        break;
                    case 'auth/invalid-email':
                        this.message = 'Correo inválido';
                        break;
                    case 'auth/user-disabled':
                        this.message = 'Cuenta deshabilitada';
                        break;
                    case 'auth/too-many-requests':
                        this.message = 'Demasiados intentos. Intenta más tarde';
                        break;
                    default:
                        this.message = 'Error en el login';
                }
                
                this.token = null;
                return { success: false };
            }
        },
        async socialLoginWithGoogle(): Promise<{ success: boolean; code?: string }> {
            try {
                const auth = getAuth();
                const provider = new GoogleAuthProvider();

                const result = await signInWithPopup(auth, provider);
                const user = result.user;

                // Obtener token Firebase (ID token)
                const token = await user.getIdToken();
                this.token = token;
                this.isVerified = true; // Con Google el correo ya está verificado
                this.pendingVerification = false;
                this.tempEmail = user.email;
                this.message = 'Login con Google exitoso';
                return { success: true };
            } catch (error: unknown) {
                console.error('Error en login Google:', error);
                const firebaseError = error as { code?: string; message?: string };

                switch (firebaseError.code) {
                    case 'auth/popup-closed-by-user':
                        this.message = 'Ventana cerrada antes de completar el login';
                        break;
                    case 'auth/cancelled-popup-request':
                        this.message = 'Solicitud de popup cancelada';
                        break;
                    case 'auth/account-exists-with-different-credential':
                        this.message = 'La cuenta existe con otra credencial. Usa el método correcto.';
                        break;
                    case 'auth/popup-blocked':
                        this.message = 'Popup bloqueado por el navegador';
                        break;
                    case 'auth/network-request-failed':
                        this.message = 'Error de red. Intenta nuevamente';
                        break;
                    default:
                        this.message = `Error en login Google: ${firebaseError.message || 'Desconocido'}`;
                }
                return { success: false, code: firebaseError.code };
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
        async generateOtp(email: string) {
            try {
                const auth = getAuth();
                const user = auth.currentUser;
                
                if (user && user.email === email) {
                    // Reenviar email de verificación
                    await sendEmailVerification(user);
                    this.message = 'Email de verificación reenviado';
                    return true;
                } else {
                    this.message = 'No se encontró usuario activo';
                    return false;
                }
            } catch (error: unknown) {
                console.error('Error al reenviar verificación:', error);
                this.message = 'Error al reenviar email de verificación';
                return false;
            }
        },
        async verifyOtp(_email: string, _otp: string) {
            try {
                const auth = getAuth();
                
                // Recargar usuario actual para obtener el estado actualizado de emailVerified
                await auth.currentUser?.reload();
                
                const user = auth.currentUser;
                
                if (user && user.emailVerified) {
                    // Obtener token de Firebase
                    const token = await user.getIdToken();
                    
                    this.token = token;
                    this.isVerified = true;
                    this.pendingVerification = false;
                    this.tempEmail = null;
                    this.message = 'Cuenta verificada';
                    
                    return true;
                } else {
                    this.message = 'Email aún no verificado. Por favor revisa tu correo y haz clic en el enlace de verificación.';
                    return false;
                }
            } catch (error: unknown) {
                console.error('Error al verificar:', error);
                this.message = 'Error al verificar la cuenta';
                return false;
            }
        }


    }


})

export default userAuth;