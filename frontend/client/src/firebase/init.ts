import { initializeApp, getApps } from 'firebase/app'

const firebaseConfig = {
  apiKey: process.env.VUE_APP_FIREBASE_API_KEY,
  authDomain: process.env.VUE_APP_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.VUE_APP_FIREBASE_PROJECT_ID,
  storageBucket: process.env.VUE_APP_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.VUE_APP_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.VUE_APP_FIREBASE_APP_ID,
  measurementId: process.env.VUE_APP_FIREBASE_MEASUREMENT_ID
}

export function ensureFirebaseInitialized(): boolean {
  try {
    if (getApps().length === 0) {
      if (!firebaseConfig.apiKey) {
        // No API key available — skip initialization
        console.warn('[Firebase Init] API key missing, skipping initializeApp')
        return false
      }
      initializeApp(firebaseConfig)
      console.info('[Firebase Init] initializeApp() completed')
      return true
    }
    return true
  } catch (e) {
    console.error('[Firebase Init] Error initializing Firebase:', e)
    return false
  }
}
