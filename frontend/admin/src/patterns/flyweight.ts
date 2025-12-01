// Re-exportación consolidada y compatibilidad con API anterior del patrón Flyweight
// Permite importar desde '@/patterns/flyweight'

import { BicycleFactory, BicycleMarker } from './BicycleFlyweight'
import { StationFactory, StationMarker } from './StationFlyweight'

// =======================
// Tipos legacy (mock) usados por DashboardLayout y componentes actuales
// =======================

export interface Bike {
	id: string
	condition: string
	model: string
	type: 'electrica' | 'mecanica' | 'electric' | 'mechanical'
	battery?: number
	latitude: number
	longitude: number
	isLocked: boolean
	getLockStatus(): string
	getLockIcon(): string
	getIcon(): string
}

export interface ReservedSlot {
	bikeId: string
	bikeName: string
	eta: string
}

export interface Station {
	id: string
	name: string
	location: string
	latitude: number
	longitude: number
	category: string
	cctvActive: boolean
	lightingActive: boolean
	panicButtonActive: boolean
	bikes: Bike[]
	reservedSlots: ReservedSlot[]
	maxCapacity: number
	// Métodos consultados por componentes
	getCapacityStatus(): string
	getLockedBikes(): number
	getTravelingBikes(): number
	getReservedSlots(): number
	getAvailableBikes(): number
	getTotalOccupied(): number
	getIcon(): string
	reserveSlot(bike: Bike, eta: string): void
}

// =======================
// Helpers legacy (factories simples para mock data)
// =======================

export function createBike(
	id: string,
	condition: string,
	model: string,
	type: 'electrica' | 'mecanica' | 'electric' | 'mechanical',
	battery: number | undefined,
	latitude: number,
	longitude: number,
	isLocked: boolean
): Bike {
	return {
		id,
		condition,
		model,
		type,
		battery,
		latitude,
		longitude,
		isLocked,
		getLockStatus() {
			return this.isLocked ? 'Locked' : 'Traveling'
		},
		getLockIcon() {
			return this.isLocked ? '🔒' : '🔓'
		},
		getIcon() {
			return this.type === 'electrica' || this.type === 'electric' ? '⚡' : '🚲'
		}
	}
}

export function createStation(
	id: string,
	name: string,
	location: string,
	latitude: number,
	longitude: number,
	category: string,
	cctvActive: boolean,
	lightingActive: boolean,
	panicButtonActive: boolean,
	bikes: Bike[]
): Station {
	const station: Station = {
		id,
		name,
		location,
		latitude,
		longitude,
		category,
		cctvActive,
		lightingActive,
		panicButtonActive,
		bikes,
		reservedSlots: [],
		// Ajuste de capacidad: al menos 12 slots o (bicicletas + reservas + 2)
		maxCapacity: Math.max(12, bikes.length + 2),
		getCapacityStatus() {
			return `${this.getTotalOccupied()}/${this.maxCapacity}`
		},
		getLockedBikes() {
			return this.bikes.filter(b => b.isLocked).length
		},
		getTravelingBikes() {
			return this.bikes.filter(b => !b.isLocked).length
		},
		getReservedSlots() {
			return this.reservedSlots.length
		},
		getAvailableBikes() {
			return this.maxCapacity - this.getTotalOccupied()
		},
		getTotalOccupied() {
			return this.bikes.length + this.reservedSlots.length
		},
		getIcon() {
			const map: Record<string, string> = {
				metro: 'location_city',
				residencial: 'home',
				'centro financiero': 'payments'
			}
			return map[this.category] || 'location_on'
		},
		reserveSlot(bike: Bike, eta: string) {
			this.reservedSlots.push({ bikeId: bike.id, bikeName: bike.model, eta })
		}
	}
	return station
}

// =======================
// Adaptadores para factories STOMP (nuevas) con API de métricas esperada
// =======================

export { BicycleFactory, BicycleMarker, StationFactory, StationMarker }

export const bikeFlyweightFactory = new BicycleFactory()
export const stationFlyweightFactory = new StationFactory()

// Métodos de compatibilidad (estadísticas) utilizados en DashboardLayout
;(bikeFlyweightFactory as unknown as { getFlyweightCount: () => number; listFlyweights: () => void }).getFlyweightCount = () => bikeFlyweightFactory.size()
;(bikeFlyweightFactory as unknown as { getFlyweightCount: () => number; listFlyweights: () => void }).listFlyweights = () => {
	console.log('🔍 Bicicletas (marcadores) en pool:', bikeFlyweightFactory.getAllMarkers().map(m => m.getId()))
}
;(stationFlyweightFactory as unknown as { getFlyweightCount: () => number; listFlyweights: () => void }).getFlyweightCount = () => stationFlyweightFactory.size()
;(stationFlyweightFactory as unknown as { getFlyweightCount: () => number; listFlyweights: () => void }).listFlyweights = () => {
	console.log('🔍 Estaciones (marcadores) en pool:', stationFlyweightFactory.getAllMarkers().map(m => m.getId()))
}
