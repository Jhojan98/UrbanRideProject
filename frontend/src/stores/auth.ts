import { defineStore } from "pinia";

const userAuth = defineStore("auth", {

    state() {
        return {
            token: null,
            baseURL: 'localhost:5001',
            message: ''
        }
    },
    actions: {

        async register(name: string, email: string, password: string, role="user") {

            const uri = `${this.baseURL}/auth/register`;
            const rawResponse = await fetch(uri, {
                method:'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    'name': name,
                    'email': email,
                    'password': password,
                    'role': role
                })
            })
            const response = await rawResponse.json();
            if (response.status != 200) {
                this.message = response.detail.msg;
                return false;
            }
            else {
                return true;
            }

        },

        async login(email: string, password: string) { 
            const uri = `${this.baseURL}/auth/login`;
            const rawResponse = await fetch(uri, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    'email': email,
                    'password':password
                })
            })
            const response = await rawResponse.json()
            if (response.status != 200) {
                this.message = response.detail.msg;
                this.token = null;
                return false;
            }
            else {
                this.token = response.token;
                return true;
            }
        },
        async logout() {
            this.token = null;
        },
        async generateOtp(email: string) {
            const uri = `${this.baseURL}/auth/generate_otp`;
            const rawResponse = await fetch(uri, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    'email': email
                })
            })
            const response = await rawResponse.json();
            if (response.status != 200) {
                this.message = response.detail.msg;
                return false;
            }
            else {
                return true;
            }
        },
        async verifyOtp(email: string, otp: number) {
            const uri = `${this.baseURL}/auth/verify_otp`;
            const rawResponse = await fetch(uri, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    'email': email,
                    'otp': otp
                })
            })
            const response = await rawResponse.json();
            if (response.status != 200) {
                this.message = response.detail.msg;
                return false;
            }
            else {
                return true;
            }
        }


    }


})

export default userAuth;