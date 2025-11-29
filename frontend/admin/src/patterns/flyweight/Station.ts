import { StationFlyweight } from './StationFlyweight'
import { Bike } from './Bike'

// Interfaz para slots reservados
export interface ReservedSlot {
    bikeId: string
    bikeName: string
    estimatedArrival?: string
}

// Parte extrínseca (única) de cada estación
export class Station {
    public reservedSlots: ReservedSlot[] = [] // Slots reservados para bicicletas en tránsito

    constructor(
        public readonly id: string,
        public name: string,
        public location: string,
        public latitude: number,
        public longitude: number,
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
        const occupied = this.bikes.length + this.reservedSlots.length
        const percentage = Math.round((occupied / this.maxCapacity) * 100)
        return `${occupied}/${this.maxCapacity} (${percentage}%)`
    }

    hasAvailableSpace(): boolean {
        return (this.bikes.length + this.reservedSlots.length) < this.maxCapacity
    }

    getReservedSlots(): number {
        return this.reservedSlots.length
    }

    getTotalOccupied(): number {
        return this.bikes.length + this.reservedSlots.length
    }

    getAvailableBikes(): number {
        return this.bikes.filter(b => b.isAvailableForRent()).length
    }

    getLockedBikes(): number {
        return this.bikes.filter(b => b.isLocked).length
    }

    getTravelingBikes(): number {
        return this.bikes.filter(b => !b.isLocked).length
    }

    // Métodos para gestionar slots reservados
    reserveSlot(bike: Bike, estimatedArrival?: string): boolean {
        if (!this.hasAvailableSpace()) {
            return false
        }
        this.reservedSlots.push({
            bikeId: bike.id,
            bikeName: `${bike.model} ${bike.getIcon()}`,
            estimatedArrival
        })
        return true
    }

    releaseReservedSlot(bikeId: string): boolean {
        const index = this.reservedSlots.findIndex(slot => slot.bikeId === bikeId)
        if (index !== -1) {
            this.reservedSlots.splice(index, 1)
            return true
        }
        return false
    }

    hasReservedSlot(bikeId: string): boolean {
        return this.reservedSlots.some(slot => slot.bikeId === bikeId)
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
            latitude: this.latitude,
            longitude: this.longitude,
            cctvActive: this.cctvActive,
            panicButtonActive: this.panicButtonActive,
            lightingActive: this.lightingActive,
            category: this.category,
            maxCapacity: this.maxCapacity,
            bikes: this.bikes.map(b => b.toJSON()),
            reservedSlots: this.reservedSlots
        }
    }
}
