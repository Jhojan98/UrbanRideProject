# Arquitectura de Internacionalización (i18n)

## Estructura General

El sistema de i18n del proyecto está organizado de forma modular y centralizado:

```
src/lang/
├── messages.ts          ← Punto de entrada ÚNICO para i18n
├── I18N_ARCHITECTURE.md ← Este documento
├── en/                  ← Fuentes de verdad para traducciones en inglés
│   ├── index.ts        ← Agrupa todos los módulos EN (opcional)
│   ├── common.ts
│   ├── auth.ts
│   ├── subscription.ts  ← Módulo modularizado (fuente de verdad)
│   └── ... (otros módulos)
└── es/                  ← Fuentes de verdad para traducciones en español
    ├── index.ts        ← Agrupa todos los módulos ES (opcional)
    ├── common.ts
    ├── auth.ts
    ├── subscription.ts  ← Módulo modularizado (fuente de verdad)
    └── ... (otros módulos)
```

## Flujo de Acceso

### ✅ CORRECTO: Acceso a través de messages.ts
```
Componente Vue → useI18n() → messages (main.ts) → messages.ts
```

**Cómo funciona:**
1. `main.ts` importa solo `messages` desde `@/lang/messages.ts`
2. `messages.ts` es el **punto de entrada único** que:
   - Importa los módulos modularizados de `en/` y `es/`
   - Los integra en una estructura única usando spread operator (`...subscriptionES`)
   - Exporta el objeto `messages` compilado
3. Los componentes Vue acceden a través de `useI18n()` que usa el objeto `messages`

### ❌ INCORRECTO: Acceso directo a módulos
```
Componente Vue → useI18n() → en/subscription.ts (PROHIBIDO)
```

Los componentes **NUNCA** deben importar directamente de `en/` o `es/`.

## Responsabilidades de Cada Archivo

### `messages.ts` - Orquestador Central
- **Responsabilidad:** Integrar todos los módulos de traducción
- **Lo que hace:**
  - Importa módulos de `en/` y `es/`
  - Crea la estructura final de mensajes
  - Exporta `messages` para `main.ts`
- **Cambios:** Solo cuando se agregan nuevos módulos de traducción

### `en/` y `es/` - Fuentes de Verdad
- **Responsabilidad:** Mantener traducciones por dominio (auth, payments, subscription, etc.)
- **Lo que hace:**
  - Cada archivo modular exporta un objeto con sus traducciones
  - Ejemplo: `subscription.ts` contiene todas las claves de `subscription.*`
- **Cambios:** Cuando se actualizan, agregan o eliminan traducciones

### `main.ts` - Inicializador de i18n
- **Responsabilidad:** Configurar vue-i18n
- **Lo que hace:**
  - Importa `messages` desde `@/lang/messages`
  - Crea la instancia de i18n con los mensajes
  - La aplica a la app de Vue
- **Cambios:** Solo para modificar configuración de i18n

## Ventajas de Esta Arquitectura

1. **Modularidad**: Cada dominio (subscription, auth, etc.) en su archivo
2. **Mantenibilidad**: Fácil encontrar y actualizar traducciones
3. **Centralización**: Un único punto de entrada (`messages.ts`)
4. **Escalabilidad**: Agregar nuevos módulos es simple
5. **Compilación**: Todas las claves se resuelven en tiempo de compilación

## Cómo Agregar una Nueva Traducción Modularizada

### Paso 1: Crear archivos modulares
```typescript
// en/newFeature.ts
export default {
    newFeature: {
        title: 'New Feature',
        description: 'Description of the new feature'
    }
}

// es/newFeature.ts
export default {
    newFeature: {
        title: 'Nueva Característica',
        description: 'Descripción de la nueva característica'
    }
}
```

### Paso 2: Importar en messages.ts
```typescript
import newFeatureES from './es/newFeature'
import newFeatureEN from './en/newFeature'

export const messages = {
    es: {
        ...otherModules,
        ...newFeatureES,  // Agregar aquí
    },
    en: {
        ...otherModules,
        ...newFeatureEN,  // Agregar aquí
    }
}
```

### Paso 3: Usar en componentes
```typescript
const { t } = useI18n()

// Accede automáticamente a través de messages.ts
const title = t('newFeature.title')
```

## Verificación de la Arquitectura

Para verificar que la arquitectura es correcta:

```bash
# 1. Verificar que no hay imports directos a en/ o es/ en componentes
grep -r "from.*lang/en/" src/components/
grep -r "from.*lang/es/" src/components/

# 2. Verificar que main.ts solo importa de messages.ts
grep -r "from.*lang/" src/main.ts

# 3. Verificar que messages.ts importa módulos
grep -r "from.*en/" src/lang/messages.ts
grep -r "from.*es/" src/lang/messages.ts
```

## Notas Importantes

- ⚠️ **NUNCA** importes directamente de `en/subscription.ts` o similar en componentes
- ✅ Siempre usa `useI18n()` que accede a `messages`
- ✅ Las claves de traducción se validan en tiempo de compilación
- ✅ El proyecto es agnóstico a la estructura interna de `messages.ts`
