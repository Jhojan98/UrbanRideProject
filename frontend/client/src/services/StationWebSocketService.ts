import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { type Station } from '@/models/Station';
import { StationFactory } from '@/patterns/StationFlyweight';
import { useStationStore } from '@/stores/station';

/**
 * WebSocket service for real-time bike availability updates.
 *
 * Stations are loaded from the store (static data: location, name, etc).
 * WebSocket only updates bike counts via /topic/station.update/user
 *
 * Flow:
 * 1. connect() -> establishes STOMP connection over SockJS
 * 2. subscribe() -> subscribes to /topic/station.update/user
 * 3. handleTelemetry() -> updates bike counts from received messages
 */
export class StationWebSocketService {
  private client: Client | null = null;
  private factory: StationFactory;
  private isConnected = false;
  private stationsCache: Map<number, Station> = new Map();
  private stationStore = useStationStore();

  private onUpdate?: (station: Station, factory: StationFactory) => void;

  constructor(factory?: StationFactory){
    this.factory = factory || new StationFactory();
  }

  connect(onUpdate?: (station: Station, factory: StationFactory)=>void){
    this.onUpdate = onUpdate;

    // Direct connection to stations service (not via gateway)
    // By default point to `estaciones-service` on port 8005 (docker-compose exposes it this way)
    const baseUrl = process.env.VUE_APP_WEBSOCKET_STATIONS_URL || 'http://localhost:8005';
    // Direct connection to stations microservice: SockJS endpoint `/ws`
    const wsUrl = `${baseUrl}/ws`;
    console.log('[Stations WS] Connecting to', wsUrl);

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as WebSocket,
      debug: str => console.log('[STOMP Stations]', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 5000,
      heartbeatOutgoing: 5000,
      onConnect: () => {
        console.log('[Stations WS] Connected');
        this.isConnected = true;
        this.subscribe();
      },
      onStompError: frame => {
        console.error('[Stations WS] STOMP error:', frame.headers['message']);
        console.error('[Stations WS] Body:', frame.body);
        this.isConnected = false;
      },
      onWebSocketClose: () => {
        console.warn('[Stations WS] Connection closed');
        this.isConnected = false;
      },
      onWebSocketError: err => {
        console.error('[Stations WS] WebSocket error:', err);
        this.isConnected = false;
      }
    });
    this.client.activate();
  }

  private subscribe(){
    if(!this.client){ console.error('STOMP not initialized'); return; }

    // Real-time telemetry: updates bike availability
    this.client.subscribe('/topic/station.update/user', msg => this.handleTelemetry(msg));

    console.log('[Stations WS] Subscriptions made');
  }

  private handleTelemetry(message: IMessage){
    try {
      // Parse telemetry data from /topic/station.update/user
      const telemetry = JSON.parse(message.body) as {
        idStation?: number;
        timestamp?: number;
        availableElectricBikes?: number;
        availableMechanicBikes?: number;
      };

      const idStation = telemetry.idStation;
      if (!idStation) {
        console.warn('[Stations WS] Telemetry sin idStation válido, se ignora');
        return;
      }

      // Get station from cache OR from store
      let station = this.stationsCache.get(idStation);

      if (!station) {
        // Try to get from store if not in cache
        const storeStation = this.stationStore?.getStationById(idStation);
        if (!storeStation) {
          console.warn(`[Stations WS] Estación ${idStation} no encontrada en cache ni store, se ignora`);
          return;
        }
        // Use store station and cache it
        station = storeStation;
        this.stationsCache.set(idStation, station);
      }

      // Update only bike counts
      station.electric = telemetry.availableElectricBikes ?? station.electric;
      station.mechanical = telemetry.availableMechanicBikes ?? station.mechanical;
      station.availableElectricBikes = telemetry.availableElectricBikes ?? station.availableElectricBikes;
      station.availableMechanicBikes = telemetry.availableMechanicBikes ?? station.availableMechanicBikes;
      // Sincronizar availableSlots con la suma de bicis reportadas (se usa para icono/popup)
      if (station.availableElectricBikes !== undefined || station.availableMechanicBikes !== undefined) {
        const mech = station.availableMechanicBikes ?? station.mechanical ?? 0;
        const elec = station.availableElectricBikes ?? station.electric ?? 0;
        station.availableSlots = mech + elec;
      }
      station.timestamp = new Date(telemetry.timestamp ?? Date.now());

      // Propagate telemetry to Pinia store so dropdowns/reservations use fresh data
      try {
        this.stationStore?.updateStationTelemetry(idStation, {
          availableElectricBikes: telemetry.availableElectricBikes,
          availableMechanicBikes: telemetry.availableMechanicBikes,
          timestamp: telemetry.timestamp
        });
      } catch (e) {
        console.warn('[Stations WS] No se pudo actualizar store con telemetría', e);
      }

      // Update the marker in factory to reflect changes
      const marker = this.factory.getMarkerById(idStation);
      if (marker) {
        marker.update(station);
      }

      // Notify listeners
      if(this.onUpdate){ this.onUpdate(station, this.factory); }
      console.log(`[Stations WS] Telemetry station ${idStation}: ⚡ ${station.electric}, ⚙️ ${station.mechanical}`);
    } catch(e){
      console.error('[Stations WS] Error parsing telemetry:', e, 'payload:', message.body);
    }
  }

  disconnect(){
    if(this.client && this.isConnected){
      console.log('[Stations WS] Disconnecting...');
      this.client.deactivate();
    }
    this.isConnected = false;
  }

  // State helpers
  getIsConnected(){ return this.isConnected; }
  getFactory(){ return this.factory; }
  getStationCount(){ return this.factory.size(); }
  getStations(): Station[] { return Array.from(this.stationsCache.values()); }
  getStation(id: number): Station | undefined { return this.stationsCache.get(id); }
}
