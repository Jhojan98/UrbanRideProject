import { Bike } from './Bike'
import { BikeFlyweightFactory } from './BikeFlyweight'
import { Station } from './Station'
import { StationFlyweightFactory } from './StationFlyweight'

export { Bike } from './Bike'
export { Station } from './Station'
export { BikeFlyweight, BikeFlyweightFactory } from './BikeFlyweight'
export { StationFlyweight, StationFlyweightFactory } from './StationFlyweight'

// Instancias singleton de las factories
export const bikeFlyweightFactory = new BikeFlyweightFactory()
export const stationFlyweightFactory = new StationFlyweightFactory()

// Helper functions to create bikes and stations
export function createBike(
    id: string,
    condition: 'Optimal' | 'Needs maintenance',
    modelo: string,
    tipo: 'electrica' | 'mecanica',
    battery?: number
): Bike {
    const flyweight = bikeFlyweightFactory.getFlyweight(modelo, tipo)
    return new Bike(id, condition, battery, flyweight)
}

export function createStation(
    id: string,
    name: string,
    location: string,
    categoria: 'metro' | 'centro financiero' | 'residencial',
    cctvActive: boolean,
    panicButtonActive: boolean,
    lightingActive: boolean,
    bikes: Bike[]
): Station {
    const flyweight = stationFlyweightFactory.getFlyweight(categoria)
    return new Station(
        id,
        name,
        location,
        cctvActive,
        panicButtonActive,
        lightingActive,
        bikes,
        flyweight
    )
}
