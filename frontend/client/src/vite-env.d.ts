/// <reference types="vite/client" />

declare namespace NodeJS {
  interface ProcessEnv {
    readonly VUE_APP_FIREBASE_API_KEY: string
    readonly VUE_APP_FIREBASE_AUTH_DOMAIN: string
    readonly VUE_APP_FIREBASE_PROJECT_ID: string
    readonly VUE_APP_FIREBASE_STORAGE_BUCKET: string
    readonly VUE_APP_FIREBASE_MESSAGING_SENDER_ID: string
    readonly VUE_APP_FIREBASE_APP_ID: string
    readonly VUE_APP_FIREBASE_MEASUREMENT_ID: string
  }
}
