import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { type Station } from '@/models/Station';
import { StationFactory } from '@/patterns/StationFlyweight';

export interface AdminStationUpdate {
  stationId: number;
  timestamp: string;
  availableElectricBikes: number;
  availableMechanicBikes: number;
  cctvStatus: boolean;
  panicButtonStatus: boolean;
  lightingStatus: boolean;
}

/**
 * Servicio WebSocket para recibir actualizaciones administrativas de estaciones.
 * Flujo:
 * 1. connect() -> establece conexión STOMP sobre SockJS
 * 2. Suscripción única a /topic/station.update/admin
 * 3. Actualiza cache local y notifica callback
 */
export class StationWebSocketService {
  private client: Client | null = null;
  private factory: StationFactory;
  private isConnected = false;
  private stationsCache: Map<number, Station> = new Map();

  private onAdminUpdate?: (stationId: number, adminData: AdminStationUpdate) => void;

  constructor(factory?: StationFactory){
    this.factory = factory || new StationFactory();
  }

  connect(onAdminUpdate?: (stationId: number, adminData: AdminStationUpdate)=>void){
    this.onAdminUpdate = onAdminUpdate;

    // Conexión directa al microservicio de estaciones (no vía gateway)
    const baseUrl = process.env.VUE_APP_WEBSOCKET_STATIONS_URL || 'http://34.9.26.232:8005';
    const wsUrl = `${baseUrl}/ws`;
    console.log('[Stations WS] Conectando a', wsUrl);

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as WebSocket,
      debug: str => console.log('[STOMP Stations]', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 5000,
      heartbeatOutgoing: 5000,
      onConnect: () => {
        console.log('[Stations WS] Conectado');
        this.isConnected = true;
        this.subscribe();
      },
      onStompError: frame => {
        console.error('[Stations WS] STOMP error:', frame.headers['message']);
        console.error('[Stations WS] Body:', frame.body);
        this.isConnected = false;
      },
      onWebSocketClose: () => {
        console.warn('[Stations WS] Conexión cerrada');
        this.isConnected = false;
      },
      onWebSocketError: err => {
        console.error('[Stations WS] Error WebSocket:', err);
        this.isConnected = false;
      }
    });
    this.client.activate();
  }

  private subscribe(){
    if(!this.client){ console.error('STOMP no inicializado'); return; }

    this.client.subscribe('/topic/station.update/admin', msg => this.handleAdminUpdate(msg));

    console.log('[Stations WS] Suscripción a /topic/station.update/admin realizada');
  }

  registerStation(station: Station){
    this.stationsCache.set(station.idStation, station);
  }

  registerStations(stations: Station[]){
    stations.forEach(st => this.stationsCache.set(st.idStation, st));
  }

  private handleAdminUpdate(message: IMessage){
    try {
      const raw = message.body;
      const adminData = JSON.parse(raw) as Record<string, unknown>;

      const pickNumber = (keys: string[]): number | null => {
        for (const k of keys) {
          const v = adminData[k];
          if (typeof v === 'number') return v;
          if (typeof v === 'string' && v.trim() !== '' && !Number.isNaN(Number(v))) return Number(v);
        }
        return null;
      };

      const pickBoolean = (keys: string[], fallback: boolean): boolean => {
        for (const k of keys) {
          const v = adminData[k];
          if (typeof v === 'boolean') return v;
          if (typeof v === 'string') {
            const val = v.toLowerCase();
            if (val === 'true') return true;
            if (val === 'false') return false;
          }
          if (typeof v === 'number') return v !== 0;
        }
        return fallback;
      };

      // Resolver id con múltiples posibles claves
      const stationId = pickNumber(['stationId', 'idStation', 'station_id', 'id']);

      console.log('[Stations WS] Mensaje admin recibido:', {
        stationId,
        adminData
      });

      if (!stationId) {
        console.warn('[Stations WS] stationId no encontrado en payload, body:', raw);
        return;
      }

      const station = this.stationsCache.get(Number(stationId));

      const update: AdminStationUpdate = {
        stationId: Number(stationId),
        timestamp: typeof adminData.timestamp === 'string' ? adminData.timestamp : new Date().toISOString(),
        availableElectricBikes: pickNumber(['availableElectricBikes', 'available_electric_bikes', 'available_electric'])
          ?? station?.availableElectricBikes
          ?? 0,
        availableMechanicBikes: pickNumber(['availableMechanicBikes', 'available_mechanic_bikes', 'available_mechanic'])
          ?? station?.availableMechanicBikes
          ?? 0,
        cctvStatus: pickBoolean(['cctvStatus', 'cctv_status'], station?.cctvStatus ?? false),
        panicButtonStatus: pickBoolean(['panicButtonStatus', 'panic_button_status'], station?.panicButtonStatus ?? false),
        lightingStatus: pickBoolean(['lightingStatus', 'lighting_status'], station?.lightingStatus ?? false)
      };

      if(station){
        station.availableElectricBikes = update.availableElectricBikes;
        station.availableMechanicBikes = update.availableMechanicBikes;
        station.cctvStatus = update.cctvStatus;
        station.panicButtonStatus = update.panicButtonStatus;
        station.lightingStatus = update.lightingStatus;
        this.factory.getStationMarker(station);
      } else {
        console.warn('[Stations WS] Estación no registrada en cache para id', stationId);
      }

      if(this.onAdminUpdate){
        this.onAdminUpdate(Number(stationId), update);
      }

      console.log(`[Stations WS] Admin update estación ${stationId}`);
    } catch(e){
      console.error('[Stations WS] Error parse admin update:', e, 'payload:', message.body);
    }
  }

  disconnect(){
    if(this.client && this.isConnected){
      console.log('[Stations WS] Desconectando...');
      this.client.deactivate();
    }
    this.isConnected = false;
  }

  // Helpers de estado
  getIsConnected(){ return this.isConnected; }
  getFactory(){ return this.factory; }
  getStationCount(){ return this.factory.size(); }
  getStations(): Station[] { return Array.from(this.stationsCache.values()); }
  getStation(id: number): Station | undefined { return this.stationsCache.get(id); }
}
