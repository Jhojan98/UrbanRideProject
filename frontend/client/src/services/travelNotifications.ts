import { defineStore } from 'pinia';

export interface NotificationData {
  type: 'EXPIRED_TRAVEL' | 'START_TRAVEL' | 'END_TRAVEL';
  message: string;
  timestamp: number;
}

export const useTripStore = defineStore('trip', {
  state: () => ({
    baseURL: process.env.VUE_APP_API_URL || 'http://localhost:8090',
    message: '',
    notification: null as NotificationData | null,
    isVisible: false,
    eventSource: null as EventSource | null,
    isConnected: false,
    connectionAttempts: 0,
    maxReconnectAttempts: 10,
    reconnectDelay: 3000, // 3 segundos
    reconnectTimer: null as number | null,
    heartbeatTimer: null as number | null,
    heartbeatTimeoutMs: 60000, // si no hay mensajes en 60s, forzar reconnect
    lifecycleListenersAttached: false,
  }),

  actions: {
    attachLifecycleListeners() {
      if (this.lifecycleListenersAttached) return;
      // Retry when coming back online
      window.addEventListener('online', () => {
        if (!this.isConnected && !this.eventSource) {
          console.log('[SSE] Online: attempting to reconnect now');
          this.connectToSSE();
        }
      });
      // Retry when returning to tab
      document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'visible' && !this.isConnected && !this.eventSource) {
          console.log('[SSE] Tab visible: attempting to reconnect now');
          this.connectToSSE();
        }
      });
      this.lifecycleListenersAttached = true;
    },

    resetHeartbeat() {
      if (this.heartbeatTimer) {
        clearTimeout(this.heartbeatTimer);
        this.heartbeatTimer = null;
      }
      this.heartbeatTimer = window.setTimeout(() => {
        console.warn('[SSE] Sin mensajes en ventana de heartbeat. Reintentando...');
        // Cerrar fuente y reintentar
        try { this.eventSource?.close(); } catch (e) { /* noop */ }
        this.eventSource = null;
        this.isConnected = false;
        this.attemptReconnect();
      }, this.heartbeatTimeoutMs);
    },

    clearHeartbeatTimer() {
      if (this.heartbeatTimer !== null) {
        clearTimeout(this.heartbeatTimer);
        this.heartbeatTimer = null;
      }
    },

    async connectToSSE() {
      // Avoid multiple simultaneous connections
      if (this.eventSource) {
        console.log('[SSE] Already has an active connection');
        return;
      }

      try {
        console.log(`[SSE] Attempting connection (attempt ${this.connectionAttempts + 1}/${this.maxReconnectAttempts})...`);

        this.eventSource = new EventSource(`${this.baseURL}/travel/sse/connect`, { withCredentials: true });

        this.eventSource.onopen = () => {
          console.log('[SSE] Connection opened successfully');
          this.isConnected = true;
          this.connectionAttempts = 0;
          this.clearReconnectTimer();
          this.resetHeartbeat();
        };

        this.eventSource.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            this.message = data.message;

            console.log('[SSE] Message received:', data.message);

            // Restart heartbeat on receiving any message
            this.resetHeartbeat();

            // Parsear el mensaje para determinar el tipo
            const messageStr = data.message || '';
            let notificationType: NotificationData['type'] | null = null;

            if (messageStr.includes('EXPIRED_TRAVEL')) {
              notificationType = 'EXPIRED_TRAVEL';
            } else if (messageStr.includes('START_TRAVEL')) {
              notificationType = 'START_TRAVEL';
            } else if (messageStr.includes('END_TRAVEL')) {
              notificationType = 'END_TRAVEL';
            }

            if (notificationType) {
              this.showNotification(notificationType, messageStr);
            }
          } catch (error) {
            console.error('[SSE] Error parsing SSE message:', error);
          }
        };

        this.eventSource.onerror = (error) => {
          console.error('[SSE] Connection error:', error);
          this.isConnected = false;
          this.clearHeartbeatTimer();
          this.eventSource?.close();
          this.eventSource = null;
          this.attemptReconnect();
        };
      } catch (error) {
        console.error('[SSE] Error creating EventSource:', error);
        this.isConnected = false;
        this.attemptReconnect();
      }
    },

    attemptReconnect() {
      // Limpiar timer previo
      this.clearReconnectTimer();

      if (this.connectionAttempts >= this.maxReconnectAttempts) {
        console.error('[SSE] Máximo número de intentos de reconexión alcanzado');
        return;
      }

      this.connectionAttempts++;
      const delay = this.reconnectDelay * Math.pow(1.5, this.connectionAttempts - 1); // Backoff exponencial

      console.log(`[SSE] Reintentando conexión en ${Math.round(delay / 1000)}s...`);

      this.reconnectTimer = window.setTimeout(() => {
        this.connectToSSE();
      }, delay);
    },

    clearReconnectTimer() {
      if (this.reconnectTimer !== null) {
        clearTimeout(this.reconnectTimer);
        this.reconnectTimer = null;
      }
    },

    showNotification(type: NotificationData['type'], message: string) {
      this.notification = {
        type,
        message,
        timestamp: Date.now(),
      };
      this.isVisible = true;
    },

    closeNotification() {
      this.isVisible = false;
      // Clear notification after animation
      setTimeout(() => {
        this.notification = null;
      }, 300);
    },

    disconnect() {
      console.log('[SSE] Disconnecting SSE');
      this.isConnected = false;
      this.clearReconnectTimer();
      this.clearHeartbeatTimer();
      if (this.eventSource) {
        this.eventSource.close();
        this.eventSource = null;
      }
    },

    getConnectionStatus() {
      return {
        isConnected: this.isConnected,
        connectionAttempts: this.connectionAttempts,
        maxReconnectAttempts: this.maxReconnectAttempts,
        status: this.isConnected ? 'Connected' : 'Disconnected',
      };
    },
  },
})
