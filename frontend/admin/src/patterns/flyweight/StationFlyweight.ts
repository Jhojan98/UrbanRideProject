// Parte intrínseca (compartida) de las estaciones basada en su tipo/categoría
export class StationFlyweight {
    // All stations now share a fixed capacity of 15 bikes
    public readonly capacidadMaxima: number = 15
    constructor(
        public readonly categoria: 'metro' | 'centro financiero' | 'residencial',
        // original capacity argument removed to enforce uniform capacity
        public readonly tieneMantenimiento24h: boolean
    ) {}

    getIcono(): string {
        switch (this.categoria) {
            case 'metro': return 'metro'
            case 'centro financiero': return 'corporate_fare'
            case 'residencial': return 'holiday_village'
        }
    }

    getDescripcionCapacidad(): string {
        return `Capacidad: hasta ${this.capacidadMaxima} bicicletas`
    }
}

// Factory para gestionar los flyweights de estaciones
export class StationFlyweightFactory {
    private flyweights: Map<string, StationFlyweight> = new Map()

    constructor() {
        // Pre-crear los tipos comunes de estaciones con las categorías correctas
        this.flyweights.set('metro', new StationFlyweight('metro', true))
        this.flyweights.set('centro financiero', new StationFlyweight('centro financiero', true))
        this.flyweights.set('residencial', new StationFlyweight('residencial', false))
    }

    getFlyweight(categoria: 'metro' | 'centro financiero' | 'residencial'): StationFlyweight {
        const flyweight = this.flyweights.get(categoria)
        if (!flyweight) {
            throw new Error(`Categoría de estación no válida: ${categoria}`)
        }
        return flyweight
    }

    getFlyweightCount(): number {
        return this.flyweights.size
    }
}
