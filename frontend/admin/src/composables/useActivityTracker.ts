import { onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

export function useActivityTracker() {
  const authStore = useAuthStore();
  const router = useRouter();
  let activityTimer: number | null = null;
  let expirationCheckInterval: number | null = null;

  // Debounce para no actualizar en cada evento
  const handleActivity = () => {
    if (activityTimer) {
      clearTimeout(activityTimer);
    }

    activityTimer = window.setTimeout(() => {
      authStore.renewActivity();
    }, 1000); // Actualizar después de 1 segundo de la última actividad
  };

  // Verificar expiración periódicamente
  const checkExpiration = () => {
    if (authStore.checkTokenExpiration()) {
      // Token expirado, redirigir a login
      router.push({ name: 'login' });
    }
  };

  onMounted(() => {
    // Escuchar eventos de actividad del usuario
    window.addEventListener('mousedown', handleActivity);
    window.addEventListener('keydown', handleActivity);
    window.addEventListener('scroll', handleActivity);
    window.addEventListener('touchstart', handleActivity);

    // Verificar expiración cada minuto
    expirationCheckInterval = window.setInterval(checkExpiration, 60000); // 60 segundos
  });

  onUnmounted(() => {
    // Limpiar listeners al desmontar
    window.removeEventListener('mousedown', handleActivity);
    window.removeEventListener('keydown', handleActivity);
    window.removeEventListener('scroll', handleActivity);
    window.removeEventListener('touchstart', handleActivity);

    if (activityTimer) {
      clearTimeout(activityTimer);
    }

    if (expirationCheckInterval) {
      clearInterval(expirationCheckInterval);
    }
  });
}
