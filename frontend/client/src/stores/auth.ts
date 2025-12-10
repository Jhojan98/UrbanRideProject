import { defineStore } from "pinia";
import {
  getAuth,
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  signOut,
  sendEmailVerification,
  GoogleAuthProvider,
  signInWithPopup,
  onAuthStateChanged,
} from "firebase/auth";
import { useTripStore } from "../services/travelNotifications";

// Inactivity timeout: 10 minutes in milliseconds
const INACTIVITY_TIMEOUT = 10 * 60 * 1000;

const userAuth = defineStore("auth", {
  state() {
    // Check if session expired due to inactivity
    const lastActivity = localStorage.getItem('lastActivity');
    if (lastActivity) {
      const timeSinceLastActivity = Date.now() - parseInt(lastActivity, 10);
      if (timeSinceLastActivity > INACTIVITY_TIMEOUT) {
        // Session expired, clear storage
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUid');
        localStorage.removeItem('lastActivity');
        console.log('Session expired due to inactivity');
      }
    }

    return {
      token: null as string | null,
      uid: null as string | null,
      baseURL: process.env.VUE_APP_API_URL || "/api",
      message: "",
      isVerified: false,
      pendingVerification: false,
      tempEmail: null as string | null,
      authStateInitialized: false,
      lastActivity: lastActivity ? parseInt(lastActivity, 10) : null as number | null,
    };
  },
  actions: {
    /**
     * Initialize authentication state from Firebase
     * Restores session if a previous one exists
     */
    async initializeAuthState() {
      return new Promise<void>((resolve) => {
        const auth = getAuth();

        // Restore token and uid from localStorage if they exist
        const savedToken = localStorage.getItem('authToken');
        const savedUid = localStorage.getItem('authUid');
        if (savedToken) {
          this.token = savedToken;
          console.log('Token restored from localStorage');
        }
        if (savedUid) {
          this.uid = savedUid;
          console.log('Uid restored from localStorage');
        }

        // Listen for authentication state changes in Firebase
        onAuthStateChanged(auth, async (user) => {
          if (user) {
            // Authenticated user
            try {
              const token = await user.getIdToken();
              this.token = token;
              this.uid = user.uid;
              this.isVerified = user.emailVerified;

              // Save token and uid in localStorage
              localStorage.setItem('authToken', token);
              localStorage.setItem('authUid', user.uid);
              console.log('Token and uid saved in localStorage');
            } catch (error) {
              console.error('Error getting token:', error);
              this.token = null;
              this.uid = null;
            }
          } else {
            // User not authenticated
            this.token = null;
            this.uid = null;
            this.isVerified = false;
            localStorage.removeItem('authToken');
            localStorage.removeItem('authUid');
          }

          this.authStateInitialized = true;
          resolve();
        });
      });
    },
    async register(username: string, email: string, password: string) {
      try {
        const auth = getAuth();

        const actionCodeSettings = {
          url: "http://localhost:8080/login",
          handleCodeInApp: true,
        };

        const userCredential = await createUserWithEmailAndPassword(
          auth,
          email,
          password
        );

        await sendEmailVerification(userCredential.user, actionCodeSettings);

        this.tempEmail = email;
        this.pendingVerification = true;
        this.isVerified = false;
        this.message =
          "Registration successful. Check your email to verify your account.";

        // Get creation date from Firebase
        const creationTime = userCredential.user.metadata.creationTime;

        console.log("=== SENDING DATA TO BACKEND (REGISTER) ===");
        console.log("uId:", userCredential.user.uid);
        console.log("username:", username);
        console.log("email:", email);
        console.log("creationTime:", creationTime);
        console.log("============================================");

        // Gateway routes /user/register → usuario-service /register
        const uri = `${this.baseURL}/user/register`;
        const rawResponse = await fetch(uri, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify({
            uidUser: userCredential.user.uid,
            userName: username,
            userEmail: userCredential.user.email,
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
      } catch (error: unknown) {
        console.error("Error in registration (full detail):", error);

        const firebaseError = error as { code?: string; message?: string };

        // Detailed logging for debugging
        console.error("Error code:", firebaseError.code);
        console.error("Error message:", firebaseError.message);

        return false;
      }
    },

    async login(
      email: string,
      password: string
    ): Promise<{
      success?: boolean;
      needsVerification?: boolean;
      userData?: unknown;
    }> {
      try {
        const auth = getAuth();

        // Sign in with Firebase
        const userCredential = await signInWithEmailAndPassword(
          auth,
          email,
          password
        );

        // Check if email is verified
        if (!userCredential.user.emailVerified) {
          this.tempEmail = email;
          this.pendingVerification = true;
          this.isVerified = false;
          this.message = "Please verify your email before signing in";

          // Offer email verification resend
          await sendEmailVerification(userCredential.user);

          return { needsVerification: true };
        }

        // Get Firebase token
        const token = await userCredential.user.getIdToken();

        console.log("=== SENDING DATA TO BACKEND (LOGIN) ===");
        console.log("email:", email);
        console.log("token:", token ? "Token obtained" : "No token");
        console.log(token);
        console.log("=========================================");

        // Send credentials to backend
        const uri = `${this.baseURL}/user/login/${userCredential.user.uid}`;
        const rawResponse = await fetch(uri, {
          method: "GET",
          headers: {
            Accept: "application/json",
            Authorization: `Bearer ${token}`,
          },
        });

        if (!rawResponse.ok) {
          console.error(
            "HTTP error from backend (login):",
            rawResponse.status,
            rawResponse.statusText
          );
          this.message = `Error validating with backend: ${rawResponse.statusText}`;
          return { success: false };
        }

        const response = await rawResponse.json();
        console.log("Response from backend (login):", response);

        this.token = token;
        this.isVerified = true;
        this.pendingVerification = false;
        this.message = "Login successful";

        // Save token in localStorage for persistence
        localStorage.setItem('authToken', token);
        console.log('Token saved in localStorage after login');

        // Initialize lastActivity timestamp
        const now = Date.now().toString();
        localStorage.setItem('lastActivity', now);
        this.lastActivity = Date.now();

        return { success: true, userData: response };
      } catch (error: unknown) {
        console.error("Error en login:", error);

        const firebaseError = error as { code?: string };

        console.error("Código de error:", firebaseError.code);

        this.token = null;
        return { success: false };
      }
    },
    async socialLoginWithGoogle(): Promise<{
      success: boolean;
      code?: string;
      userData?: unknown;
    }> {
      try {
        const auth = getAuth();
        const provider = new GoogleAuthProvider();

        // Request explicit permissions for email and profile
        provider.addScope("email");
        provider.addScope("profile");

        // Configure to always request account selection
        provider.setCustomParameters({
          prompt: "select_account",
        });

        const result = await signInWithPopup(auth, provider);
        const user = result.user;

        // Ensure the email sent to backend is the same as registered in Firebase
        const userEmail =
          user.email ||
          user.providerData.find((p) => p?.email)?.email ||
          null;

        if (!userEmail) {
          this.message = "Could not obtain email from Google account";
          console.error("Google login without available email", {
            uid: user.uid,
            providerData: user.providerData,
          });
          return { success: false, code: "missing-email" };
        }

        // Get Firebase token (ID token)
        const token = await user.getIdToken();

        console.log("=== SENDING DATA TO BACKEND (GOOGLE LOGIN) ===");
        console.log("uId:", user.uid);
        console.log("email:", userEmail);
        console.log("displayName:", user.displayName);
        console.log("creationTime:", user.metadata.creationTime);
        console.log("================================================");

        // First check if user already exists
        const checkUri = `${this.baseURL}/user/login/${user.uid}`;
        const checkResponse = await fetch(checkUri, {
          method: "GET",
          headers: {
            Accept: "application/json",
            Authorization: `Bearer ${token}`,
          },
        });

        let userData;
        if (checkResponse.ok) {
          // User exists, just get data
          userData = await checkResponse.json();
          console.log("Existing user found:", userData);
        } else if (checkResponse.status === 404) {
          // User does not exist, register
          console.log("User not found, registering...");
          const registerUri = `${this.baseURL}/user/register`;
          const registerResponse = await fetch(registerUri, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Accept: "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
              uidUser: user.uid,
              userName:
                user.displayName ||
                userEmail.split("@")[0] || `user_${user.uid.substring(0, 6)}`,
              userEmail: userEmail,
            }),
          });

          if (!registerResponse.ok) {
            console.error(
              "Error HTTP al registrar (Google):",
              registerResponse.status,
              registerResponse.statusText
            );
            const errorText = await registerResponse.text();
            console.error("Respuesta del servidor:", errorText);
            this.message = `Error al registrar en backend: ${registerResponse.statusText}`;
            return { success: false };
          }

          userData = await registerResponse.json();
          console.log("Usuario registrado exitosamente:", userData);
        } else {
          console.error(
            "Error inesperado al verificar usuario:",
            checkResponse.status,
            checkResponse.statusText
          );
          this.message = `Error verifying user: ${checkResponse.statusText}`;
          return { success: false };
        }

        this.token = token;
        this.isVerified = true; // With Google the email is already verified
        this.pendingVerification = false;
        this.tempEmail = userEmail;
        this.message = "Google login successful";

        // Save token in localStorage for persistence
        localStorage.setItem('authToken', token);
        console.log('Token saved in localStorage after Google login');

        // Initialize lastActivity timestamp
        const now = Date.now().toString();
        localStorage.setItem('lastActivity', now);
        this.lastActivity = Date.now();

        return { success: true, userData };
      } catch (error: unknown) {
        console.error("Error in Google login:", error);
        const firebaseError = error as { code?: string; message?: string };

        switch (firebaseError.code) {
          case "auth/popup-closed-by-user":
            this.message = "Ventana cerrada antes de completar el login";
            break;
          case "auth/cancelled-popup-request":
            this.message = "Solicitud de popup cancelada";
            break;
          case "auth/account-exists-with-different-credential":
            this.message =
              "La cuenta existe con otra credencial. Usa el método correcto.";
            break;
          case "auth/popup-blocked":
            this.message = "Popup bloqueado por el navegador";
            break;
          case "auth/network-request-failed":
            this.message = "Error de red. Intenta nuevamente";
            break;
          default:
            this.message = `Error en login Google: ${
              firebaseError.message || "Desconocido"
            }`;
        }
        return { success: false, code: firebaseError.code };
      }
    },
    async logout() {
      try {
        const auth = getAuth();
        await signOut(auth);
        this.token = null;
        this.uid = null;
        this.isVerified = false;
        this.message = "Sesión cerrada";
        this.lastActivity = null;

        // Disconnect SSE when logging out
        const tripStore = useTripStore();
        tripStore.disconnect();

        // Clear all stored auth data
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUid');
        localStorage.removeItem('lastActivity');
      } catch (error: unknown) {
        console.error("Error en logout:", error);
        this.message = "Error al cerrar sesión";
      }
    },
    async generateOtp(email: string) {
      try {
        const auth = getAuth();
        const user = auth.currentUser;

        if (user && user.email === email) {
          // Resend verification email
          await sendEmailVerification(user);
          this.message = "Verification email resent";
          return true;
        } else {
          this.message = "No active user found";
          return false;
        }
      } catch (error: unknown) {
        console.error("Error resending verification:", error);
        this.message = "Error resending verification email";
        return false;
      }
    },
    async verifyOtp(_email: string, _otp: string) {
      try {
        const auth = getAuth();

        // Reload current user to get updated emailVerified status
        await auth.currentUser?.reload();

        const user = auth.currentUser;

        if (user && user.emailVerified) {
          // Get Firebase token
          const token = await user.getIdToken();

          this.token = token;
          this.isVerified = true;
          this.pendingVerification = false;
          this.tempEmail = null;
          this.message = "Account verified";

          // Save token in localStorage for persistence
          localStorage.setItem('authToken', token);
          console.log('Token saved in localStorage after verification');

          // Initialize lastActivity timestamp
          const now = Date.now().toString();
          localStorage.setItem('lastActivity', now);
          this.lastActivity = Date.now();

          return true;
        } else {
          this.message =
            "Email aún no verificado. Por favor revisa tu correo y haz clic en el enlace de verificación.";
          return false;
        }
      } catch (error: unknown) {
        console.error("Error al verificar:", error);
        this.message = "Error al verificar la cuenta";
        return false;
      }
    },
        async buySubscription() {
          try {
            const url = `${this.baseURL}/user/subscription/purchase/${this.uid}`;
            const res = await fetch(url, {
              method: "POST",
              headers: {
                Accept: "application/json",
                Authorization: `Bearer ${this.token}`,
              },
            });
            if (!res.ok) {
              const txt = await res.text().catch(() => "");
              throw new Error(`HTTP ${res.status} ${res.statusText} ${txt}`);
            }
            const data = await res.json();
            console.log("Subscription purchase response:", data);
            return data;
          }
          catch (error: unknown) {
            console.error("Error al comprar suscripción:", error);
          }
    },
    /**
     * Renew activity timestamp
     * Called by activity tracker on user interaction
     */
    renewActivity() {
      const now = Date.now().toString();
      localStorage.setItem('lastActivity', now);
      this.lastActivity = Date.now();
    },
    /**
     * Check if session has expired due to inactivity
     * If expired, logout the user
     * @returns true if session expired and user was logged out
     */
    checkTokenExpiration(): boolean {
      const lastActivity = localStorage.getItem('lastActivity');
      if (!lastActivity) {
        return false;
      }

      const timeSinceLastActivity = Date.now() - parseInt(lastActivity, 10);
      if (timeSinceLastActivity > INACTIVITY_TIMEOUT) {
        console.log('Session expired due to inactivity, logging out...');
        this.logout();
        return true;
      }
      return false;
    },
  },
});

export default userAuth;
