import { StationFlyweight } from './StationFlyweight'
import { Bike } from './Bike'

// Parte extrínseca (única) de cada estación
export class Station {
    constructor(
        public readonly id: string,
        public name: string,
        public location: string,
        public cctvActive: boolean,
        public panicButtonActive: boolean,
        public lightingActive: boolean,
        public bikes: Bike[],
        private flyweight: StationFlyweight
    ) {}

    // Acceso a propiedades del flyweight
    get category(): 'metro' | 'centro financiero' | 'residencial' {
        return this.flyweight.categoria
    }

    get maxCapacity(): number {
        return this.flyweight.capacidadMaxima
    }

    get has24hMaintenance(): boolean {
        return this.flyweight.tieneMantenimiento24h
    }

    getIcon(): string {
        return this.flyweight.getIcono()
    }

    // Métodos que operan con estado extrínseco e intrínseco
    getCapacityStatus(): string {
        const occupied = this.bikes.length
        const percentage = Math.round((occupied / this.maxCapacity) * 100)
        return `${occupied}/${this.maxCapacity} (${percentage}%)`
    }

    hasAvailableSpace(): boolean {
        return this.bikes.length < this.maxCapacity
    }

    getAvailableBikes(): number {
        return this.bikes.filter(b => !b.needsMaintenance()).length
    }

    needsAttention(): boolean {
        // Verificar si hay problemas de seguridad
        if (!this.cctvActive || !this.lightingActive) {
            return true
        }
        // Verificar si está muy llena o muy vacía
        const occupancy = (this.bikes.length / this.maxCapacity) * 100
        if (occupancy > 90 || occupancy < 10) {
            return true
        }
        return false
    }

    toJSON() {
        return {
            id: this.id,
            name: this.name,
            location: this.location,
            cctvActive: this.cctvActive,
            panicButtonActive: this.panicButtonActive,
            lightingActive: this.lightingActive,
            category: this.category,
            maxCapacity: this.maxCapacity,
            bikes: this.bikes.map(b => b.toJSON())
        }
    }
}
