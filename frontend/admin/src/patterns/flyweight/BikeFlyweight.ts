// Parte intrínseca (compartida) de las bicicletas
export class BikeFlyweight {
    constructor(
        public readonly modelo: string,
        public readonly tipo: 'electrica' | 'mecanica'
    ) {}

    // Métodos que operan sobre el estado compartido
    getIcon(): string {
        return this.tipo === 'electrica' ? '⚡' : '🚲'
    }

    getCapacidadBateria(): number {
        return this.tipo === 'electrica' ? 100 : 0
    }
}

// Factory para gestionar y reutilizar los flyweights
export class BikeFlyweightFactory {
    private flyweights: Map<string, BikeFlyweight> = new Map()

    getKey(modelo: string, tipo: 'electrica' | 'mecanica'): string {
        return `${modelo}-${tipo}`
    }

    getFlyweight(modelo: string, tipo: 'electrica' | 'mecanica'): BikeFlyweight {
        const key = this.getKey(modelo, tipo)
        
        if (!this.flyweights.has(key)) {
            console.log(`🔨 Creando nuevo BikeFlyweight: ${key}`)
            this.flyweights.set(key, new BikeFlyweight(modelo, tipo))
        } else {
            console.log(`♻️ Reutilizando BikeFlyweight: ${key}`)
        }
        
        const flyweight = this.flyweights.get(key)
        if (!flyweight) {
            throw new Error(`Error al obtener flyweight: ${key}`)
        }
        return flyweight
    }

    getFlyweightCount(): number {
        return this.flyweights.size
    }

    listFlyweights(): void {
        console.log(`\nTotal de Flyweights creados: ${this.flyweights.size}`)
        this.flyweights.forEach((flyweight, key) => {
            console.log(`  - ${key}`)
        })
    }
}
