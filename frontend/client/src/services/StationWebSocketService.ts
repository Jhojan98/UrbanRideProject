import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Station, StationWithSlotsDTO, toStationWithSlots } from '@/models/Station';
import { StationFactory } from '@/patterns/StationFlyweight';

/**
 * Servicio WebSocket para manejo de estaciones y sus slots.
 * Flujo:
 * 1. connect() -> establece conexión STOMP sobre SockJS
 * 2. requestInitialLoad() -> envía mensaje a /app/stations.request para bulk inicial
 * 3. Suscripciones:
 *    - /topic/stations.bulk   (array completo de estaciones con slots)
 *    - /topic/station.update  (actualizaciones incrementales de una estación)
 */
export class StationWebSocketService {
  private client: Client | null = null;
  private factory: StationFactory;
  private isConnected = false;
  private hasBulk = false;
  private stationsCache: Map<number, Station> = new Map();

  private onInitialLoad?: (stations: Station[], factory: StationFactory) => void;
  private onUpdate?: (station: Station, factory: StationFactory) => void;

  constructor(factory?: StationFactory){
    this.factory = factory || new StationFactory();
  }

  connect(onInitialLoad?: (stations: Station[], factory: StationFactory)=>void, onUpdate?: (station: Station, factory: StationFactory)=>void){
    this.onInitialLoad = onInitialLoad;
    this.onUpdate = onUpdate;

    const baseUrl = process.env.VUE_APP_WEBSOCKET_STATIONS_URL || 'http://localhost:8090';
    const wsUrl = `${baseUrl}/ws/estaciones/ws`;
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
        // Solicitar carga inicial automática
        this.requestInitialLoad();
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

    // Bulk inicial
    this.client.subscribe('/topic/stations.bulk', msg => this.handleBulk(msg));
    // Actualizaciones individuales
    this.client.subscribe('/topic/station.update', msg => this.handleUpdate(msg));

    console.log('[Stations WS] Suscripciones realizadas');
  }

  private handleBulk(message: IMessage){
    try {
      const arr: StationWithSlotsDTO[] = JSON.parse(message.body);
      const stations: Station[] = arr.map(toStationWithSlots);
      stations.forEach(st => {
        this.stationsCache.set(st.idStation, st);
        this.factory.getStationMarker(st); // actualiza/crea en pool
      });
      this.hasBulk = true;
      if(this.onInitialLoad){ this.onInitialLoad(stations, this.factory); }
      console.log(`[Stations WS] Bulk recibido (${stations.length} estaciones)`);
    } catch(e){
      console.error('[Stations WS] Error parse bulk:', e, 'payload:', message.body);
    }
  }

  private handleUpdate(message: IMessage){
    try {
      const dto: StationWithSlotsDTO = JSON.parse(message.body);
      const station = toStationWithSlots(dto);
      this.stationsCache.set(station.idStation, station);
      this.factory.getStationMarker(station);
      if(this.onUpdate){ this.onUpdate(station, this.factory); }
      console.log(`[Stations WS] Update estación ${station.idStation}`);
    } catch(e){
      console.error('[Stations WS] Error parse update:', e, 'payload:', message.body);
    }
  }

  requestInitialLoad(){
    if(!this.client || !this.isConnected){
      console.warn('[Stations WS] No conectado, no se puede solicitar bulk');
      return;
    }
    this.client.publish({ destination: '/app/stations.request', body: JSON.stringify({ action: 'load_all' }) });
    console.log('[Stations WS] Solicitud bulk enviada');
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
  getHasInitialBulk(){ return this.hasBulk; }
  getFactory(){ return this.factory; }
  getStationCount(){ return this.factory.size(); }
  getStations(): Station[] { return Array.from(this.stationsCache.values()); }
  getStation(id: number): Station | undefined { return this.stationsCache.get(id); }
}
