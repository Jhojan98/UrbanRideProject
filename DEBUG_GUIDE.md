# Guía de Debugging - Flujo de Reserva de Viajes

## Problema Identificado

Error: `[ConfirmationComponent] Error confirmando reserva: Error: Estación de origen no especificada`

**Causa Raíz:** Los datos de la reserva no se estaban guardando correctamente porque:

1. `ReservationView` no pasaba la prop `station` a `ReserveFormComponent`
2. `ReserveFormComponent` guardaba `props.station` que era `undefined`, cuando debería guardar `props.origin`
3. El balance del usuario no se mostraba en la UI

## Cambios Realizados

### 1. ReservationView.vue

**Cambio:** Ahora pasa `station` y `balance` a `ReserveFormComponent`

```vue
<ReserveFormComponent
  :station="origin"           <!-- Estación de origen seleccionada -->
  :balance="userBalance"      <!-- Saldo del usuario -->
  :origin="origin"
  :destination="destination"
  @update:origin="(o) => origin = o"
  @update:destination="(d) => destination = d"
/>
```

**Adiciones:**

- `userBalance` ref inicializado con 50000 (valor por defecto)
- TODO: Obtener balance real desde userStore cuando esté disponible

### 2. ReserveFormComponent.vue

**Cambios:**

- `reserve()` ahora usa `props.origin?.idStation` en lugar de `props.station?.idStation`
- `onConfirmRoute()` usa `payload.origin` en lugar de `props.station`
- Se agregó logging detallado para seguimiento

```typescript
// Antes (INCORRECTO):
const stationStartId = props.origin?.idStation ?? props.station?.idStation ?? null
const reservationPayload = {
  station: props.station  // ❌ undefined
}

// Ahora (CORRECTO):
const stationStartId = props.origin?.idStation ?? null
const reservationPayload = {
  station: props.origin  // ✅ tiene datos
}
```

### 3. useReservation.ts

**Cambios:**

- Tipo actualizado: `station?: Partial<Station> | null` (acepta null)
- Se agregó logging en `setReservation()`:
  - Imprime la reserva completa
  - Imprime la estación guardada
  - Imprime el idStation para verificación

```typescript
const setReservation = (data: ReservationData) => {
  console.log('[useReservation] Guardando reserva:', data);
  console.log('[useReservation] Station guardada:', data.station);
  console.log('[useReservation] Station idStation:', data.station?.idStation);
  reservationData.value = data
  isReservationActive.value = true
}
```

### 4. ConfirmationComponent.vue

**Cambios:**

- Se agregó logging exhaustivo al inicio de `confirmReservation()`
- Imprime el estado actual de `reservationData` y `hasActiveReservation`
- Imprime los datos de station antes de validar

## Flujo de Datos - Vista Completa

```
ReservationView
├─ origin (Endpoint | null)
├─ destination (Endpoint | null)
├─ userBalance (number = 50000)
│
└─→ ReserveFormComponent
    ├─ props.station = origin
    ├─ props.balance = userBalance
    ├─ props.origin = origin
    ├─ props.destination = destination
    │
    ├─→ UltimaMilla (selector de origen/destino)
    │
    └─→ reserve()
        ├─ Obtiene userUid de Firebase
        ├─ Extrae idStation de props.origin y props.destination
        ├─ Llama a travelStore.initiateTravel()
        └─→ setReservation({
              bikeType: string
              rideType: string
              station: props.origin  ✅ (tiene idStation)
              startResponse: respuesta del backend
            })
            │
            └─→ ConfirmationComponent
                ├─ Accede a reservationData.value
                ├─ Valida station?.idStation (ahora existe)
                ├─ Valida startResponse (confirmación del backend)
                └─ Confirma la reserva
```

## Cómo Probar

### Pasos para Test Manual

1. **Abre la consola del navegador** (F12 → Console)

2. **Navega a la vista de reserva** (/reservation)

3. **Verifica que el balance se muestre**
   - Busca el texto "Balance:" en el formulario
   - Debería mostrar el saldo (por defecto: $50000)

4. **Selecciona un tipo de bicicleta**
   - Mecánica o Eléctrica

5. **Selecciona un tipo de viaje**
   - Última Milla (short_trip) o Recorrido Largo (long_trip)

6. **En la consola, verifica los logs iniciales**

   Busca:
   ```
   [UltimaMilla] Tipos de estaciones disponibles:
   [UltimaMilla] - Estación X: type=METRO
   [UltimaMilla] - Estación Y: type=RESIDENCIAL
   ```

7. **Selecciona origen y destino** desde los dropdowns

8. **Haz clic en "Reservar"** o **"Confirmar Ruta"** (según tipo de viaje)

9. **En la consola, deberías ver**

   ```
   [ReserveFormComponent] Iniciando viaje con datos: {
     userUid: "xxx",
     stationStartId: 123,
     stationEndId: 456,
     bikeTypeMapped: "MECHANIC" o "ELECTRIC"
   }
   [ReserveFormComponent] Respuesta del backend: {...}
   [useReservation] Guardando reserva: {
     bikeType: "mechanical" o "electric",
     rideType: "short_trip" o "long_trip",
     station: {
       idStation: 123,
       nameStation: "Estación X",
       ...
     },
     startResponse: {...}
   }
   [useReservation] Station guardada: {...}
   [useReservation] Station idStation: 123
   ```

10. **Se debería mostrar ConfirmationComponent**

11. **Al hacer clic en "Confirmar"**, en la consola verás

    ```
    [ConfirmationComponent] confirmReservation - Estado actual:
    [ConfirmationComponent] - reservationData: {
      bikeType: "...",
      rideType: "...",
      station: { idStation: 123, ... },
      startResponse: {...}
    }
    [ConfirmationComponent] - hasActiveReservation: true
    [ConfirmationComponent] Confirmando reserva: {...}
    ```

12. **Si todo va bien**
    - Se mostrará alerta "Reserva confirmada"
    - Se redirigirá a home (/)

## Puntos de Control de Debugging

### Si ves: "Estación de origen no especificada"

Busca en consola y verifica:

1. **¿Se llama a `setReservation()`?**
   - Busca: `[useReservation] Guardando reserva:`
   - Si no aparece → El botón reserve() o onConfirmRoute() no se ejecutó

2. **¿Tiene `station` la reserva guardada?**
   - Busca: `[useReservation] Station guardada:`
   - Si muestra `undefined` → Se está pasando mal desde ReserveFormComponent

3. **¿Tiene `idStation` la station?**
   - Busca: `[useReservation] Station idStation:`
   - Si muestra `undefined` → El origen seleccionado no tiene `idStation`

4. **Verifica que origin tenga datos**
   - Abre DevTools → Vue → ReservationView
   - Verifica que `origin` tenga valores (no sea null)

### Si el balance no se muestra

1. Verifica que `ReservationView` tenga:

```typescript
const userBalance = ref<number>(50000)
```

2. Verifica que `ReserveFormComponent` tenga:

```vue
<div class="balance-container">
  {{ $t('reservation.form.balance') }} <strong>${{ props.balance }}</strong>
</div>
```

## Próximas Mejoras

- [ ] Obtener `userBalance` desde userStore en lugar de hardcodear
- [ ] Mostrar balance desde Firebase/backend
- [ ] Validar saldo suficiente antes de confirmar reserva
- [ ] Mostrar estimado de costo en ConfirmationComponent
- [ ] Test end-to-end automático

## Referencias

- Composable: `src/composables/useReservation.ts`
- Vista: `src/views/ReservationView.vue`
- Componentes: `src/components/reservation/*.vue`
- Store: `src/stores/travel.ts`
