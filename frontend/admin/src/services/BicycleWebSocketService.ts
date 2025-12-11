import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * Interfaz para las actualizaciones de ubicación y batería de bicicletas
 * desde el backend
 */
export interface BicycleLocationUpdate {
  latitude: number;
  longitude: number;
  battery: number;
  timestamp: number;
}

/**
 * Servicio WebSocket para recibir actualizaciones en tiempo real de bicicletas.
 * Flujo:
 * 1. connect() -> establece conexión STOMP sobre SockJS
 * 2. Suscripción a /topic/bicycle.location
 * 3. Recibe actualizaciones de latitud, longitud y batería
 * 4. Notifica via callback para actualizar el patrón Flyweight
 *
 * Nota: La carga inicial de bicicletas es responsabilidad del bikeStore
 */
export class BicycleWebSocketService {
  private client: Client | null = null;
  private isConnected = false;

  // Callback para notificar actualizaciones de ubicación
  private onLocationUpdate?: (bikeId: string, update: BicycleLocationUpdate) => void;

  /**
   * Establece la conexión WebSocket
   * @param onLocationUpdate Callback que se ejecuta cuando se recibe una actualización
   */
  connect(onLocationUpdate?: (bikeId: string, update: BicycleLocationUpdate) => void) {
    this.onLocationUpdate = onLocationUpdate;

    // Conexión al microservicio de bicicletas
    const baseUrl = process.env.VUE_APP_WEBSOCKET_BICYCLES_URL || 'http://34.9.26.232:8003';
    const wsUrl = `${baseUrl}/ws`;
    console.log('[Bicycles WS] Conectando a', wsUrl);

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as WebSocket,
      debug: str => console.log('[STOMP Bicycles]', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 5000,
      heartbeatOutgoing: 5000,
      onConnect: () => {
        console.log('[Bicycles WS] Conectado exitosamente');
        this.isConnected = true;
        this.subscribeToLocationUpdates();
      },
      onStompError: frame => {
        console.error('[Bicycles WS] Error STOMP:', frame.headers['message']);
        console.error('[Bicycles WS] Detalles:', frame.body);
        this.isConnected = false;
      },
      onWebSocketError: error => {
        console.error('[Bicycles WS] Error WebSocket:', error);
        this.isConnected = false;
      },
      onDisconnect: () => {
        console.log('[Bicycles WS] Desconectado');
        this.isConnected = false;
      }
    });

    this.client.activate();
  }

  /**
   * Suscripción al topic de actualizaciones de ubicación
   */
  private subscribeToLocationUpdates() {
    if (!this.client || !this.isConnected) {
      console.warn('[Bicycles WS] No se puede suscribir: cliente no conectado');
      return;
    }

    console.log('[Bicycles WS] Suscribiéndose a /topic/bicycle.location');

    this.client.subscribe('/topic/bicycle.location', (message: IMessage) => {
      try {
        const payload = JSON.parse(message.body);
        console.log('[Bicycles WS] 📨 Actualización recibida:', payload);

        // Extraer el bikeId con múltiples variantes posibles
        const bikeId = message.headers['bikeId']
          || payload.bikeId
          || payload.id
          || payload.idBicycle;

        if (!bikeId) {
          console.warn('[Bicycles WS] ⚠️ Actualización sin bikeId:', payload);
          return;
        }

        const update: BicycleLocationUpdate = {
          latitude: payload.latitude,
          longitude: payload.longitude,
          battery: payload.battery,
          timestamp: payload.timestamp || Date.now()
        };

        // Validar que los datos sean válidos
        if (
          typeof update.latitude !== 'number' ||
          typeof update.longitude !== 'number' ||
          typeof update.battery !== 'number'
        ) {
          console.error('[Bicycles WS] ❌ Datos inválidos:', update);
          return;
        }

        console.log(`[Bicycles WS] 🚲 Actualizando bicicleta ${bikeId}:`, {
          lat: update.latitude,
          lon: update.longitude,
          battery: update.battery
        });

        // Notificar actualización
        if (this.onLocationUpdate) {
          this.onLocationUpdate(String(bikeId), update);
        }
      } catch (error) {
        console.error('[Bicycles WS] ❌ Error procesando mensaje:', error);
      }
    });
  }

  /**
   * Desconecta el cliente WebSocket
   */
  disconnect() {
    if (this.client) {
      console.log('[Bicycles WS] Desconectando...');
      this.client.deactivate();
      this.client = null;
      this.isConnected = false;
    }
  }

  /**
   * Verifica si el servicio está conectado
   */
  getIsConnected(): boolean {
    return this.isConnected;
  }
}

/**
 * Instancia singleton del servicio
 */
export const bicycleWebSocketService = new BicycleWebSocketService();
