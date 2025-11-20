# Patrón Flyweight - Sistema de Gestión de Bicicletas

## Descripción

El patrón **Flyweight** se ha implementado para optimizar el uso de memoria cuando se manejan grandes cantidades de bicicletas y estaciones en el sistema. Este patrón permite compartir datos comunes (intrínsecos) entre múltiples objetos, mientras que los datos únicos (extrínsecos) se mantienen separados.

## Problema Resuelto

Con un alto volumen de bicicletas y estaciones:
- **Sin Flyweight**: Si tenemos 1000 bicicletas del modelo "UrbanX" eléctricas, cada objeto almacenaría redundantemente el modelo y tipo (≈20 KB por objeto = **20 MB**).
- **Con Flyweight**: Los 1000 objetos comparten un único `BikeFlyweight` con el modelo y tipo, reduciendo significativamente el uso de memoria.

## Estructura

### 1. BikeFlyweight (Estado Intrínseco)

**Archivo**: `src/patterns/flyweight/BikeFlyweight.ts`

```typescript
class BikeFlyweight {
  modelo: string      // Compartido entre todas las bicis del mismo modelo
  tipo: 'electrica' | 'mecanica'  // Compartido
}
```

**Datos compartidos**:
- `modelo`: UrbanX, UrbanLite, EcoRide
- `tipo`: electrica o mecanica
- Métodos que operan sobre datos compartidos

### 2. Bike (Estado Extrínseco)

**Archivo**: `src/patterns/flyweight/Bike.ts`

```typescript
class Bike {
  id: string          // Único por bicicleta
  condicion: string   // Único (Excelente, Buena, Regular, Mala)
  bateria?: number    // Único (nivel de batería)
  flyweight: BikeFlyweight  // Referencia al objeto compartido
}
```

**Datos únicos**:
- `id`: Identificador único
- `condicion`: Estado actual
- `bateria`: Nivel de carga (solo eléctricas)

### 3. BikeFlyweightFactory

**Archivo**: `src/patterns/flyweight/BikeFlyweight.ts`

Gestiona la creación y reutilización de flyweights:

```typescript
class BikeFlyweightFactory {
  getFlyweight(modelo: string, tipo: string): BikeFlyweight {
    // Crea uno nuevo solo si no existe
    // Reutiliza el existente si ya fue creado
  }
}
```

### 4. StationFlyweight (Estado Intrínseco)

**Archivo**: `src/patterns/flyweight/StationFlyweight.ts`

```typescript
class StationFlyweight {
  categoria: 'principal' | 'secundaria' | 'pequena'
  capacidadMaxima: number
  tieneMantenimiento24h: boolean
}
```

### 5. Station (Estado Extrínseco)

**Archivo**: `src/patterns/flyweight/Station.ts`

```typescript
class Station {
  id: string
  nombre: string
  ubicacion: string
  cctvActivo: boolean
  botonPanicoActivo: boolean
  iluminacionActiva: boolean
  bicicletas: Bike[]
  flyweight: StationFlyweight  // Referencia al objeto compartido
}
```

## Uso

### Crear Bicicletas

```typescript
import { crearBicicleta } from '@/patterns/flyweight'

// Estas 3 bicicletas compartirán el mismo flyweight
const bike1 = crearBicicleta('B-001', 'Excelente', 'UrbanX', 'electrica', 87)
const bike2 = crearBicicleta('B-002', 'Buena', 'UrbanX', 'electrica', 65)
const bike3 = crearBicicleta('B-003', 'Regular', 'UrbanX', 'electrica', 45)
```

### Crear Estaciones

```typescript
import { crearEstacion } from '@/patterns/flyweight'

const station = crearEstacion(
  'ST-001',
  'Estación Central',
  'Av. Principal 123',
  'principal',  // Reutiliza flyweight de categoría 'principal'
  true,
  false,
  true,
  [bike1, bike2, bike3]
)
```

## Beneficios

### 1. Reducción de Memoria

- **Antes**: 10,000 bicicletas × 50 bytes/objeto = **500 KB**
- **Después**: 10,000 referencias × 8 bytes + 10 flyweights × 50 bytes = **80.5 KB**
- **Ahorro**: ~84% de memoria

### 2. Performance

- Menos objetos = menos presión en el garbage collector
- Mejor localidad de caché
- Inicialización más rápida

### 3. Mantenibilidad

- Cambios en propiedades compartidas se reflejan en todos los objetos
- Código más organizado y fácil de entender
- Separación clara entre datos compartidos y únicos

## Estadísticas en Consola

Al iniciar la aplicación, verás:

```
🚀 Inicializando estaciones con patrón Flyweight...
🔨 Creando nuevo BikeFlyweight: UrbanX-electrica
♻️ Reutilizando BikeFlyweight: UrbanX-electrica
🔨 Creando nuevo BikeFlyweight: UrbanLite-mecanica
📊 Estadísticas de Flyweight:
   🚲 Flyweights de bicicletas creados: 3
   🏢 Flyweights de estaciones creados: 3
```

## Escalabilidad

El patrón está listo para manejar:
- ✅ Miles de bicicletas
- ✅ Cientos de estaciones
- ✅ Múltiples modelos y tipos
- ✅ Diferentes categorías de estaciones

## Archivos Modificados

1. `src/patterns/flyweight/BikeFlyweight.ts` - Flyweight de bicicletas
2. `src/patterns/flyweight/Bike.ts` - Clase Bike con flyweight
3. `src/patterns/flyweight/StationFlyweight.ts` - Flyweight de estaciones
4. `src/patterns/flyweight/Station.ts` - Clase Station con flyweight
5. `src/patterns/flyweight/index.ts` - Exportaciones y helpers
6. `src/layouts/DashboardLayout.vue` - Uso del patrón
7. `src/components/dashboard/StationInfo.vue` - Actualizado
8. `src/components/dashboard/BikeInfo.vue` - Actualizado
