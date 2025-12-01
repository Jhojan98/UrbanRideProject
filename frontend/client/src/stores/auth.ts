import { defineStore } from "pinia";
import { getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut, sendEmailVerification, GoogleAuthProvider, signInWithPopup} from 'firebase/auth';

const userAuth = defineStore("auth", {
    state() {
        return {
            token: null as string | null,
            baseURL: 'http://localhost:8090',
            message: '',
            isVerified: false,
            pendingVerification: false,
            tempEmail: null as string | null
        }
    },
    actions: {

        async register(username:string, email:string, password:string) {
            try {
                const auth = getAuth();

                const actionCodeSettings = {
                    url: 'http://localhost:8080/login',
                    handleCodeInApp: true
                }

                const userCredential = await createUserWithEmailAndPassword(auth, email, password);

                await sendEmailVerification(userCredential.user, actionCodeSettings);

                this.tempEmail = email;
                this.pendingVerification = true;
                this.isVerified = false;
                this.message = 'Registro exitoso. Revisa tu correo para verificar tu cuenta.';

                // Obtener fecha de creación de Firebase
                const creationTime = userCredential.user.metadata.creationTime;

                console.log('=== ENVIANDO DATOS AL BACKEND (REGISTER) ===');
                console.log('uId:', userCredential.user.uid);
                console.log('username:', username);
                console.log('email:', email);
                console.log('creationTime:', creationTime);
                console.log('============================================');

                // Gateway enruta /user/register → usuario-service /register
                const uri = `${this.baseURL}/user/register`;
                const rawResponse = await fetch(uri, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                    },
                    body: JSON.stringify({
                        uidUser: userCredential.user.uid,
                        userName: username
                        // Backend User solo acepta: uidUser, userName, subscriptionType, balance
                    })
                });

                if (!rawResponse.ok) {
                    console.error('Error HTTP del backend:', rawResponse.status, rawResponse.statusText);
                    const errorText = await rawResponse.text();
                    console.error('Respuesta del servidor:', errorText);
                    this.message = `Error al guardar en backend: ${rawResponse.statusText}`;
                    return false;
                }

                const response = await rawResponse.json();
                console.log('Respuesta del backend (register):', response);

                return true;

            } catch (error: unknown) {
                console.error('Error en registro (detalle completo):', error);

                const firebaseError = error as { code?: string; message?: string };

                // Log detallado para depuración
                console.error('Código de error:', firebaseError.code);
                console.error('Mensaje de error:', firebaseError.message);

                return false;
            }
        },

        async login(email: string, password: string): Promise<{ success?: boolean; needsVerification?: boolean; userData?: unknown }> {
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

                console.log('=== ENVIANDO DATOS AL BACKEND (LOGIN) ===');
                console.log('email:', email);
                console.log('token:', token ? 'Token obtenido' : 'No token');
                console.log('=========================================');

                // Enviar credenciales al backend
                const uri = `${this.baseURL}/user/login/${userCredential.user.uid}`;
                const rawResponse = await fetch(uri, {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json',
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (!rawResponse.ok) {
                    console.error('Error HTTP del backend (login):', rawResponse.status, rawResponse.statusText);
                    this.message = `Error al validar con backend: ${rawResponse.statusText}`;
                    return { success: false };
                }

                const response = await rawResponse.json();
                console.log('Respuesta del backend (login):', response);

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
        async socialLoginWithGoogle(): Promise<{ success: boolean; code?: string; userData?: unknown }> {
            try {
                const auth = getAuth();
                const provider = new GoogleAuthProvider();

                const result = await signInWithPopup(auth, provider);
                const user = result.user;

                // Obtener token Firebase (ID token)
                const token = await user.getIdToken();

                console.log('=== ENVIANDO DATOS AL BACKEND (GOOGLE LOGIN) ===');
                console.log('uId:', user.uid);
                console.log('email:', user.email);
                console.log('displayName:', user.displayName);
                console.log('creationTime:', user.metadata.creationTime);
                console.log('================================================');

                // Primero verificar si el usuario ya existe
                const checkUri = `${this.baseURL}/user/login/${user.uid}`;
                const checkResponse = await fetch(checkUri, {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json',
                        'Authorization': `Bearer ${token}`,
                    }
                });

                let userData;
                if (checkResponse.ok) {
                    // Usuario existe, solo obtener datos
                    userData = await checkResponse.json();
                    console.log('Usuario existente encontrado:', userData);
                } else if (checkResponse.status === 404) {
                    // Usuario no existe, registrar
                    console.log('Usuario no encontrado, registrando...');
                    const registerUri = `${this.baseURL}/user/register`;
                    const registerResponse = await fetch(registerUri, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Accept': 'application/json',
                            'Authorization': `Bearer ${token}`,
                        },
                        body: JSON.stringify({
                            uidUser: user.uid,
                            userName: user.displayName || (user.email ? user.email.split('@')[0] : `user_${user.uid.substring(0,6)}`)
                        })
                    });

                    if (!registerResponse.ok) {
                        console.error('Error HTTP al registrar (Google):', registerResponse.status, registerResponse.statusText);
                        const errorText = await registerResponse.text();
                        console.error('Respuesta del servidor:', errorText);
                        this.message = `Error al registrar en backend: ${registerResponse.statusText}`;
                        return { success: false };
                    }

                    userData = await registerResponse.json();
                    console.log('Usuario registrado exitosamente:', userData);
                } else {
                    console.error('Error inesperado al verificar usuario:', checkResponse.status, checkResponse.statusText);
                    this.message = `Error al verificar usuario: ${checkResponse.statusText}`;
                    return { success: false };
                }

                this.token = token;
                this.isVerified = true; // Con Google el correo ya está verificado
                this.pendingVerification = false;
                this.tempEmail = user.email;
                this.message = 'Login con Google exitoso';
                return { success: true, userData };
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