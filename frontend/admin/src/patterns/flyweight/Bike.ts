import { BikeFlyweight } from './BikeFlyweight'

// Parte extrínseca (única) de cada bicicleta
export class Bike {
    constructor(
        public readonly id: string,
        public condition: 'Optimal' | 'Needs maintenance',
        public battery: number | undefined, // Solo para eléctricas
        private flyweight: BikeFlyweight
    ) {}

    // Acceso a propiedades del flyweight
    get modelo(): string {
        return this.flyweight.modelo
    }

    get tipo(): 'electrica' | 'mecanica' {
        return this.flyweight.tipo
    }

    getIcon(): string {
        return this.flyweight.getIcon()
    }

    // Métodos que operan con estado extrínseco e intrínseco
    getStatus(): string {
        const batteryStatus = this.tipo === 'electrica' 
            ? ` (Batería: ${this.battery}%)` 
            : ''
        return `${this.modelo} ${this.getIcon()} - ${this.condition}${batteryStatus}`
    }

    needsMaintenance(): boolean {
        if (this.condition === 'Needs maintenance') {
            return true
        }
        if (this.tipo === 'electrica' && this.battery !== undefined && this.battery < 30) {
            return true
        }
        return false
    }

    toJSON() {
        return {
            id: this.id,
            condition: this.condition,
            modelo: this.modelo,
            tipo: this.tipo,
            battery: this.battery
        }
    }
}
