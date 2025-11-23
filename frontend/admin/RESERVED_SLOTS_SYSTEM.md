# Sistema de Slots Reservados

## Descripción General

Este documento describe la implementación del sistema de slots reservados para bicicletas en tránsito en la aplicación de administración de UrbanRide.

## Concepto

Las bicicletas que se encuentran en viaje (traveling) deben:
1. **Desvincularse de la estación de origen** - No aparecen en el array `bikes` de la estación donde estaban estacionadas
2. **Reservar un slot en la estación destino** - Se agregan al array `reservedSlots` de la estación a la que se dirigen

## Componentes Modificados

### 1. Station.ts (`src/patterns/flyweight/Station.ts`)

#### Nuevas Estructuras de Datos

```typescript
export interface ReservedSlot {
    bikeId: string
    bikeName: string
    estimatedArrival: string
}
```

#### Nuevas Propiedades

- `reservedSlots: ReservedSlot[]` - Array que almacena las bicicletas que van en camino a esta estación

#### Nuevos Métodos

| Método | Descripción | Retorno |
|--------|-------------|---------|
| `reserveSlot(bike, estimatedArrival)` | Reserva un slot para una bicicleta en tránsito | `boolean` |
| `releaseReservedSlot(bikeId)` | Libera un slot reservado cuando la bici llega | `boolean` |
| `hasReservedSlot(bikeId)` | Verifica si hay un slot reservado para una bici | `boolean` |
| `getReservedSlots()` | Obtiene el número de slots reservados | `number` |
| `getTotalOccupied()` | Calcula bicicletas presentes + slots reservados | `number` |

### 2. StationInfo.vue (`src/components/dashboard/StationInfo.vue`)

#### Actualización de Funciones

**`getSlotClass(station, slotNumber)`**
- Ahora distingue entre 3 tipos de slots ocupados:
  - `occupied` 🔒 - Bicicleta estacionada (fondo verde)
  - `traveling` 🔓 - Bicicleta en viaje desde esta estación (fondo naranja)
  - `reserved` 📍 - Slot reservado para bicicleta en camino (fondo morado con borde punteado)

**`getSlotTooltip(station, slotNumber)`**
- Para slots reservados muestra: `"Reservado para: [bikeName] ([bikeId])"`

#### Nuevos Estilos CSS

```scss
.slot-box.reserved { 
    background: #8b5cf6;  // Morado
    color: #fff;
    border: 2px dashed #fff;  // Borde punteado
}
```

#### Template Actualizado

```vue
<span v-else-if="n <= station.getTotalOccupied()" class="slot-lock">
    📍
</span>
```

**Resumen actualizado:**
```
🔒 Estacionadas: X | 🔓 En viaje: Y | 📍 Reservados: Z | ✅ Disponibles: W
```

### 3. MapComponent.vue (`src/components/dashboard/MapComponent.vue`)

#### Popup de Estaciones

Agregada nueva línea en el popup:
```javascript
<p style="margin: 4px 0;"><strong>📍 Reservados:</strong> ${station.getReservedSlots()}</p>
```

### 4. DashboardLayout.vue (`src/layouts/DashboardLayout.vue`)

#### Escenario de Prueba

Se implementó un escenario con 3 bicicletas en tránsito:

| Bicicleta | Origen | Destino | Tiempo Estimado | Coordenadas Actuales |
|-----------|--------|---------|-----------------|---------------------|
| B-101 (UrbanLite mecanica) | ST-001 Centro | ST-002 Parque Sikuani | 15 minutos | [4.1490, -73.6430] |
| B-202 (EcoRide electrica) | ST-002 Parque Sikuani | ST-003 Zona Universitaria | 20 minutos | [4.1545, -73.6360] |
| B-301 (UrbanX electrica) | ST-003 Zona Universitaria | ST-004 Terminal | 10 minutos | [4.1510, -73.6385] |

#### Código Implementado

```typescript
// Reservar slots para bicicletas en tránsito
const bikeB101 = createBike('B-101', 'Optimal', 'UrbanLite', 'mecanica', undefined, 4.1490, -73.6430, false)
station2.reserveSlot(bikeB101, '15 minutos')

const bikeB202 = createBike('B-202', 'Optimal', 'EcoRide', 'electrica', 95, 4.1545, -73.6360, false)
station3.reserveSlot(bikeB202, '20 minutos')

const bikeB301 = createBike('B-301', 'Optimal', 'UrbanX', 'electrica', 72, 4.1510, -73.6385, false)
station4.reserveSlot(bikeB301, '10 minutos')
```

## Estado de las Estaciones

### ST-001 - Estación Centro
- **Capacidad:** 15 slots
- **Bicicletas estacionadas:** 3 (B-100, B-102, B-103)
- **En viaje desde aquí:** 0
- **Reservados:** 0
- **Disponibles:** 3

### ST-002 - Parque Sikuani
- **Capacidad:** 8 slots
- **Bicicletas estacionadas:** 2 (B-200, B-201)
- **En viaje desde aquí:** 0
- **Reservados:** 1 (B-101 en camino - 15 min)
- **Disponibles:** 2

### ST-003 - Zona Universitaria
- **Capacidad:** 12 slots
- **Bicicletas estacionadas:** 1 (B-300)
- **En viaje desde aquí:** 0
- **Reservados:** 1 (B-202 en camino - 20 min)
- **Disponibles:** 1

### ST-004 - Terminal de Transportes
- **Capacidad:** 15 slots
- **Bicicletas estacionadas:** 2 (B-400, B-401)
- **En viaje desde aquí:** 0
- **Reservados:** 1 (B-301 en camino - 10 min)
- **Disponibles:** 2

## Visualización

### Colores de Slots

| Estado | Color | Icono | Descripción |
|--------|-------|-------|-------------|
| Ocupado | 🟢 Verde (#16a34a) | 🔒 | Bicicleta estacionada y bloqueada |
| En viaje | 🟠 Naranja (#f59e0b) | 🔓 | Bicicleta que salió de esta estación |
| Reservado | 🟣 Morado (#8b5cf6) | 📍 | Slot reservado para bicicleta en camino |
| Vacío | ⚪ Gris (#e5e7eb) | - | Slot disponible |

### Tooltips

Cuando el usuario pasa el mouse sobre un slot:

- **Slot ocupado:** `B-100 - Estacionada`
- **Slot en viaje:** `B-101 - En viaje`
- **Slot reservado:** `Reservado para: UrbanLite mecanica (B-101)`
- **Slot vacío:** `Vacío`

## Flujo de Trabajo

### 1. Usuario Renta una Bicicleta

```typescript
// Ejemplo: Usuario renta B-100 de ST-001 y va a ST-002
const bike = station1.removeBike('B-100')
bike.isLocked = false
station2.reserveSlot(bike, '15 minutos')
```

### 2. Bicicleta Llega a Destino

```typescript
// La bicicleta llega a ST-002
bike.isLocked = true
bike.latitude = station2.latitude
bike.longitude = station2.longitude
station2.addBike(bike)
station2.releaseReservedSlot(bike.id)
```

## Integración con Patrón Flyweight

El sistema de slots reservados **no afecta** el patrón Flyweight:
- Los datos compartidos (flyweight) permanecen en `BikeFlyweight` y `StationFlyweight`
- Los datos extrínsecos (estado único) se mantienen en `Bike` y `Station`
- `reservedSlots` es un dato extrínseco de cada estación

## Validación

### Pruebas Visuales

1. ✅ Los slots reservados se muestran en color morado con borde punteado
2. ✅ El tooltip muestra el ID de la bicicleta que viene en camino
3. ✅ El contador de capacidad incluye slots reservados
4. ✅ El mapa muestra la cantidad de slots reservados en los popups

### Métodos de Verificación

```typescript
// Verificar slots reservados
console.log(station2.getReservedSlots()) // 1

// Verificar ocupación total
console.log(station2.getTotalOccupied()) // bikes.length + reservedSlots.length

// Verificar si hay reserva para una bici
console.log(station2.hasReservedSlot('B-101')) // true
```

## Próximos Pasos

1. **Implementar temporizadores** - Actualizar automáticamente cuando expire el tiempo estimado
2. **Agregar notificaciones** - Alertar cuando una bicicleta está cerca de llegar
3. **Historial de reservas** - Guardar registro de reservas pasadas
4. **Cancelación de reservas** - Permitir liberar slots si el usuario cancela

---

**Fecha de implementación:** 2024-01-XX  
**Autor:** GitHub Copilot  
**Versión:** 1.0
