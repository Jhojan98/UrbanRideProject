import { defineStore } from 'pinia';

export interface NotificationData {
  type: 'EXPIRED_TRAVEL' | 'START_TRAVEL' | 'END_TRAVEL';
  message: string;
  timestamp: number;
}

export const useTripStore = defineStore('trip', {
  state: () => ({
    baseURL: '/api',
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
    userUid: null as string | null,
  }),

  actions: {
    attachLifecycleListeners() {
      if (this.lifecycleListenersAttached) return;
      console.log('[SSE] Attaching lifecycle listeners');
      // Retry when coming back online
      window.addEventListener('online', () => {
        console.log('[SSE] 🌐 Online event detected');
        if (!this.isConnected && !this.eventSource) {
          console.log('[SSE] Online: attempting to reconnect now');
          this.connectToSSE();
        }
      });
      // Retry when returning to tab
      document.addEventListener('visibilitychange', () => {
        console.log('[SSE] 👁️ Visibility change event detected, state:', document.visibilityState);
        if (document.visibilityState === 'visible' && !this.isConnected && !this.eventSource) {
          console.log('[SSE] Tab visible: attempting to reconnect now');
          this.connectToSSE();
        }
      });
      this.lifecycleListenersAttached = true;
      console.log('[SSE] ✅ Lifecycle listeners attached');
    },

    resetHeartbeat() {
      if (this.heartbeatTimer) {
        clearTimeout(this.heartbeatTimer);
        this.heartbeatTimer = null;
      }
      this.heartbeatTimer = window.setTimeout(() => {
        console.warn('═══════════════════════════════════════════');
        console.warn('[SSE] ⏱️ HEARTBEAT TIMEOUT');
        console.warn('═══════════════════════════════════════════');
        console.warn('[SSE] No messages received in 60 seconds');
        console.warn('[SSE] Connection will be closed and reconnection attempted');
        console.warn('[SSE] UID:', this.userUid);
        console.warn('═══════════════════════════════════════════');
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

      // Get user uid from localStorage or state
      if (!this.userUid) {
        this.userUid = localStorage.getItem('authUid');
        console.log('[SSE] UID loaded from localStorage:', this.userUid);
      } else {
        console.log('[SSE] UID already in state:', this.userUid);
      }

      if (!this.userUid) {
        console.warn('[SSE] No user uid available, cannot connect to SSE');
        return;
      }

      try {
        const sseUrl = `${this.baseURL}/notification/sse/connect?uid=${encodeURIComponent(this.userUid)}`;
        console.log(`[SSE] Attempting connection (attempt ${this.connectionAttempts + 1}/${this.maxReconnectAttempts})...`);
        console.log('[SSE] Base URL:', this.baseURL);
        console.log('[SSE] User UID:', this.userUid);
        console.log('[SSE] Full SSE URL:', sseUrl);

        // Crear EventSource SIN opciones (igual a SseClient.vue que funciona)
        this.eventSource = new EventSource(sseUrl);

        this.eventSource.onopen = () => {
          console.log('[SSE] ✅ Connection opened successfully');
          console.log('[SSE] Event Source state:', this.eventSource?.readyState);
          console.log('[SSE] Ready to receive messages for UID:', this.userUid);
          this.isConnected = true;
          this.connectionAttempts = 0;
          this.clearReconnectTimer();
          this.resetHeartbeat();
        };

        // Escuchar eventos con nombre 'mensaje' (como en SseClient)
        this.eventSource.addEventListener('mensaje', (event) => {
          try {
            console.log('═══════════════════════════════════════════');
            console.log('[SSE] 📨 MESSAGE RECEIVED (evento "mensaje")');
            console.log('═══════════════════════════════════════════');
            console.log('[SSE] Raw event.data:', event.data);

            let data;
            try {
              data = JSON.parse(event.data);
              console.log('[SSE] ✅ JSON parsed successfully');
              console.log('[SSE] Parsed data:', data);
              console.log('[SSE] Data keys:', Object.keys(data));
            } catch (parseError) {
              console.error('[SSE] ❌ Failed to parse JSON:', parseError);
              console.error('[SSE] Raw string:', event.data);
              throw parseError;
            }

            this.message = data.message;
            console.log('[SSE] Message extracted:', this.message);
            console.log('[SSE] Message type:', typeof this.message);

            // Restart heartbeat on receiving any message
            this.resetHeartbeat();
            console.log('[SSE] ✅ Heartbeat reset');

            // Parsear el mensaje para determinar el tipo
            const messageStr = data.message || '';
            console.log('[SSE] Message string for parsing:', messageStr);
            console.log('[SSE] Message includes EXPIRED_TRAVEL:', messageStr.includes('EXPIRED_TRAVEL'));
            console.log('[SSE] Message includes START_TRAVEL:', messageStr.includes('START_TRAVEL'));
            console.log('[SSE] Message includes END_TRAVEL:', messageStr.includes('END_TRAVEL'));

            let notificationType: NotificationData['type'] | null = null;

            // Detectar tipo con precedencia: EXPIRED > END > START
            // Usar match más estricto para evitar falsos positivos
            if (messageStr.toUpperCase().includes('EXPIRED_TRAVEL')) {
              notificationType = 'EXPIRED_TRAVEL';
              console.log('[SSE] ✅ Notification type detected: EXPIRED_TRAVEL');
            } else if (messageStr.toUpperCase().includes('END_TRAVEL')) {
              notificationType = 'END_TRAVEL';
              console.log('[SSE] ✅ Notification type detected: END_TRAVEL');
            } else if (messageStr.toUpperCase().includes('START_TRAVEL')) {
              notificationType = 'START_TRAVEL';
              console.log('[SSE] ✅ Notification type detected: START_TRAVEL');
            } else {
              console.log('[SSE] ⚠️ No notification type matched in message');
            }

            if (notificationType) {
              console.log('[SSE] 🔔 Showing notification:', { type: notificationType, message: messageStr });
              this.showNotification(notificationType, messageStr);
              console.log('[SSE] ✅ Notification displayed');
            } else {
              console.log('[SSE] ℹ️ Message received but no type matched - storing anyway');
            }
            console.log('═══════════════════════════════════════════');
          } catch (error) {
            console.error('═══════════════════════════════════════════');
            console.error('[SSE] ❌ ERROR PROCESSING MESSAGE');
            console.error('═══════════════════════════════════════════');
            console.error('[SSE] Error object:', error);
            console.error('[SSE] Error message:', (error as Error).message);
            console.error('[SSE] Error stack:', (error as Error).stack);
            console.error('[SSE] Original event.data:', event.data);
            console.error('═══════════════════════════════════════════');
          }
        });

        // Fallback: también escuchar onmessage genérico por si el backend envía sin nombre
        this.eventSource.onmessage = (event) => {
          console.log('[SSE] 📨 Generic message received (onmessage):', event.data);
          try {
            const data = JSON.parse(event.data);
            this.resetHeartbeat();
            // Procesar igual que 'mensaje'
            const messageStr = data.message || '';
            let notificationType: NotificationData['type'] | null = null;
            if (messageStr.includes('EXPIRED_TRAVEL')) notificationType = 'EXPIRED_TRAVEL';
            else if (messageStr.includes('START_TRAVEL')) notificationType = 'START_TRAVEL';
            else if (messageStr.includes('END_TRAVEL')) notificationType = 'END_TRAVEL';

            if (notificationType) {
              this.showNotification(notificationType, messageStr);
            }
          } catch (e) {
            console.error('[SSE] Error parsing generic message:', e);
          }
        };

        this.eventSource.onerror = (error) => {
          console.error('[SSE] ❌ Connection error:', error);
          console.error('[SSE] Error type:', error.type);
          console.error('[SSE] ReadyState:', this.eventSource?.readyState);

          // Si la conexión se cierra, reintentar
          if (this.eventSource?.readyState === EventSource.CLOSED) {
            console.error('[SSE] Connection closed by server, attempting reconnect');
            this.isConnected = false;
            this.clearHeartbeatTimer();
            this.eventSource?.close();
            this.eventSource = null;
            this.attemptReconnect();
          }
        };
      } catch (error) {
        console.error('[SSE] ❌ Error creating EventSource:', error);
        console.error('[SSE] Error object:', error);
        console.error('[SSE] Error message:', (error as Error).message);
        this.isConnected = false;
        this.attemptReconnect();
      }
    },

    attemptReconnect() {
      // Limpiar timer previo
      this.clearReconnectTimer();

      if (this.connectionAttempts >= this.maxReconnectAttempts) {
        console.error('═══════════════════════════════════════════');
        console.error('[SSE] 🛑 MAX RECONNECT ATTEMPTS REACHED');
        console.error('═══════════════════════════════════════════');
        console.error('[SSE] Maximum attempts:', this.maxReconnectAttempts);
        console.error('[SSE] UID:', this.userUid);
        console.error('[SSE] URL was: ${this.baseURL}/notification/sse/connect?uid=${this.userUid}');
        console.error('[SSE] Manual reconnection required');
        console.error('═══════════════════════════════════════════');
        return;
      }

      this.connectionAttempts++;
      const delay = this.reconnectDelay * Math.pow(1.5, this.connectionAttempts - 1); // Backoff exponencial

      console.log('═══════════════════════════════════════════');
      console.log('[SSE] 🔄 RECONNECTION ATTEMPT');
      console.log('═══════════════════════════════════════════');
      console.log(`[SSE] Attempt ${this.connectionAttempts}/${this.maxReconnectAttempts}`);
      console.log(`[SSE] Retrying connection in ${Math.round(delay / 1000)}s...`);
      console.log('[SSE] UID:', this.userUid);
      console.log('═══════════════════════════════════════════');

      this.reconnectTimer = window.setTimeout(() => {
        console.log('[SSE] Executing reconnection...');
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
      console.log('[SSE] Creating notification object:', { type, message, timestamp: Date.now() });
      this.notification = {
        type,
        message,
        timestamp: Date.now(),
      };
      this.isVisible = true;
      console.log('[SSE] Notification visibility set to true');
      console.log('[SSE] Current notification state:', this.notification);
    },

    closeNotification() {
      console.log('[SSE] Closing notification');
      this.isVisible = false;
      // Clear notification after animation
      setTimeout(() => {
        this.notification = null;
        console.log('[SSE] Notification cleared from state');
      }, 300);
    },

    disconnect() {
      console.log('═══════════════════════════════════════════');
      console.log('[SSE] 🔌 MANUAL DISCONNECT');
      console.log('═══════════════════════════════════════════');
      console.log('[SSE] UID:', this.userUid);
      console.log('[SSE] Connection status:', this.isConnected);
      this.isConnected = false;
      this.clearReconnectTimer();
      this.clearHeartbeatTimer();
      if (this.eventSource) {
        this.eventSource.close();
        this.eventSource = null;
        console.log('[SSE] EventSource closed');
      }
      console.log('[SSE] ✅ Disconnected successfully');
      console.log('═══════════════════════════════════════════');
    },

    getConnectionStatus() {
      const status = {
        isConnected: this.isConnected,
        connectionAttempts: this.connectionAttempts,
        maxReconnectAttempts: this.maxReconnectAttempts,
        status: this.isConnected ? 'Connected' : 'Disconnected',
        userUid: this.userUid,
        baseURL: this.baseURL,
        sseUrl: `${this.baseURL}/notification/sse/connect?uid=${this.userUid}`,
      };
      console.log('[SSE] Connection Status:', status);
      return status;
    },
  },
})
