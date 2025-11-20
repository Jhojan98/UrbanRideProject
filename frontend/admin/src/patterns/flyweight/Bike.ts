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
    get model(): string {
        return this.flyweight.modelo
    }

    get type(): 'electric' | 'mechanical' {
        return this.flyweight.tipo === 'electrica' ? 'electric' : 'mechanical'
    }

    getIcon(): string {
        return this.flyweight.getIcon()
    }

    // Métodos que operan con estado extrínseco e intrínseco
    getStatus(): string {
        const batteryStatus = this.type === 'electric' 
            ? ` (Battery: ${this.battery}%)` 
            : ''
        return `${this.model} ${this.getIcon()} - ${this.condition}${batteryStatus}`
    }

    needsMaintenance(): boolean {
        if (this.condition === 'Needs maintenance') {
            return true
        }
        if (this.type === 'electric' && this.battery !== undefined && this.battery < 30) {
            return true
        }
        return false
    }

    toJSON() {
        return {
            id: this.id,
            condition: this.condition,
            model: this.model,
            type: this.type,
            battery: this.battery
        }
    }
}
