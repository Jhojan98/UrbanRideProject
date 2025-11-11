import { defineStore } from 'pinia';

export const useTripStore = defineStore('trip', {
    state: () => ({
        startLocation: null,
        endLocation: null,
        bikeType: 'mechanical',
        tripDuration: 0
    }),
    actions: {}
})