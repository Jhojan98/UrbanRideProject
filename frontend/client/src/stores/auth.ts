import { defineStore } from "pinia";

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
            const uri = `${this.baseURL}/auth/register`;
            try {
                // Formatear fecha a YYYY-MM-DD para el backend
                const formattedDate = birthDate.toISOString().split('T')[0];
                
                const payload = {
                    k_user_cc: id,
                    n_username: username,
                    password: password,
                    n_user_first_name: fName,
                    n_user_second_name: sName || null,
                    n_user_first_lastname: fLastName,
                    n_user_second_lastname: sLastName || null,
                    f_user_birthdate: formattedDate,
                    n_user_email: email
                };
                
                const res = await fetch(uri, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                
                if (!res.ok) {
                    const err: unknown = await res.json().catch(() => ({}));
                    const detail = (err && typeof err === 'object') ? (err as Record<string, unknown>).detail : undefined;
                    this.message = (typeof detail === 'string')
                        ? detail
                        : (detail && typeof detail === 'object' && 'msg' in (detail as Record<string, unknown>))
                            ? String((detail as Record<string, unknown>).msg)
                            : 'Error en el registro';
                    return false;
                }
                // Éxito: marcar pendiente de verificación y generar OTP
                this.tempEmail = email;
                this.pendingVerification = true;
                await this.generateOtp(email); // no bloquear por errores de OTP
                this.message = 'Registro exitoso. Te enviamos un OTP';
                return true;
            } catch {
                this.message = 'Error de red en el registro';
                return false;
            }
        },

        async login(email: string, password: string) { 
            const uri = `${this.baseURL}/auth/login`;
            try {
                const res = await fetch(uri, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify({
                        username: email,
                        password: password
                    })
                });
                
                let json: unknown = null;
                try {
                    json = await res.json();
                } catch {
                    json = null;
                }

                // 403 => necesita verificación
                if (res.status === 403) {
                    this.tempEmail = email;
                    this.pendingVerification = true;
                    await this.generateOtp(email);
                    const detail403 = (json && typeof json === 'object') ? (json as Record<string, unknown>).detail : undefined;
                    this.message = (typeof detail403 === 'string') ? detail403 : 'Por favor verifica tu cuenta primero';
                    return { needsVerification: true };
                }

                // Otros errores HTTP
                if (!res.ok) {
                    const detail = (json && typeof json === 'object') ? (json as Record<string, unknown>).detail : undefined;
                    this.message = (typeof detail === 'string') ? detail : 'Credenciales inválidas';
                    this.token = null;
                    return { success: false };
                }

                // Éxito: validar estructura { access_token: string, token_type: string, is_verified: bool, expires_in: number }
                const jsonObj = json as Record<string, unknown>;
                const accessToken: unknown = jsonObj.access_token;
                const isVerified: unknown = jsonObj.is_verified;
                
                if (typeof accessToken !== 'string' || !accessToken) {
                    this.message = 'Respuesta inválida del servidor';
                    this.token = null;
                    return { success: false };
                }

                this.token = accessToken;
                this.isVerified = typeof isVerified === 'boolean' ? isVerified : true;
                this.pendingVerification = false;
                this.message = 'Login exitoso';
                return { success: true };
            } catch {
                this.message = 'Error de red en el login';
                return { success: false };
            }
        },
        async logout() {
            this.token = null;
        },
        async generateOtp(email: string) {
            const uri = `${this.baseURL}/auth/generate_otp`;
            try {
                const res = await fetch(uri, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email })
                });
                if (!res.ok) {
                    const err: unknown = await res.json().catch(() => ({}));
                    const detail = (err && typeof err === 'object') ? (err as Record<string, unknown>).detail : undefined;
                    this.message = (typeof detail === 'string')
                        ? detail
                        : (detail && typeof detail === 'object' && 'msg' in (detail as Record<string, unknown>))
                            ? String((detail as Record<string, unknown>).msg)
                            : 'Error al generar OTP';
                    return false;
                }
                this.message = 'OTP enviado';
                return true;
            } catch {
                this.message = 'Error de red al generar OTP';
                return false;
            }
        },
        async verifyOtp(email: string, otp: string) {
            const uri = `${this.baseURL}/auth/verify_otp`;
            try {
                const res = await fetch(uri, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email: email, otp: otp })
                });
                
                let json: unknown = null;
                try {
                    json = await res.json();
                } catch {
                    json = null;
                }

                if (!res.ok) {
                    const detail = (json && typeof json === 'object') ? (json as Record<string, unknown>).detail : undefined;
                    this.message = (typeof detail === 'string')
                        ? detail
                        : (detail && typeof detail === 'object' && 'msg' in (detail as Record<string, unknown>))
                            ? String((detail as Record<string, unknown>).msg)
                            : 'OTP inválido';
                    return false;
                }

                // Verificar si el servidor devuelve un token tras verificación exitosa
                const token: unknown = (json && typeof json === 'object') ? (json as Record<string, unknown>).token : undefined;
                if (typeof token === 'string' && token) {
                    // Si el servidor devuelve token, establecerlo (auto-login tras verificación)
                    this.token = token;
                }

                this.isVerified = true;
                this.pendingVerification = false;
                this.tempEmail = null;
                this.message = 'Cuenta verificada';
                return true;
            } catch {
                this.message = 'Error al verificar el código OTP';
                return false;
            }
        }


    }


})

export default userAuth;