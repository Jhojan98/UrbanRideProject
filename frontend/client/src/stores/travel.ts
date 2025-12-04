import { defineStore } from 'pinia'

export const useTravelStore = defineStore('travel', {
  state: () => ({
    baseURL: (process.env.VUE_APP_TRAVEL_BASE_URL as string) || 'http://localhost:8003'
  }),
  actions: {
    async startTravel(payload: { userUid: string; stationStartId: number; stationEndId: number; bikeType: string }) {
      const url = this.baseURL.replace(/\/$/, '') + '/start'
      const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify(payload)
      })
      if (!res.ok) {
        const txt = await res.text().catch(() => '')
        throw new Error(`HTTP ${res.status} ${res.statusText} ${txt}`)
      }
      return await res.json().catch(() => null)
    }
  }
})
