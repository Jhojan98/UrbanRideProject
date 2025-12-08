/* eslint-disable */
/**
 * Declaraciones de tipos globales para las macros de compilador de Vue 3
 * Estas macros (defineProps, defineEmits, withDefaults) están disponibles
 * automáticamente en <script setup> y no requieren importación.
 */

declare global {
  const defineProps: typeof import('vue')['defineProps']
  const defineEmits: typeof import('vue')['defineEmits']
  const defineExpose: typeof import('vue')['defineExpose']
  const withDefaults: typeof import('vue')['withDefaults']
}

export {}
