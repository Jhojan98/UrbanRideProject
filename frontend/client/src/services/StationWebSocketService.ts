import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { type Station, type StationDTO, toStation } from '@/models/Station';
import { StationFactory } from '@/patterns/StationFlyweight';
import { useStationStore } from '@/stores/station';

/**
 * WebSocket service for handling stations and their slots.
 * Flow:
 * 1. connect() -> establishes STOMP connection over SockJS
 * 2. requestInitialLoad() -> sends message to /app/stations.request for initial bulk
 * 3. Subscriptions:
 *    - /topic/stations.bulk   (complete array of stations with slots)
 *    - /topic/station.update  (incremental updates of a single station)
 */
export class StationWebSocketService {
  private client: Client | null = null;
  private factory: StationFactory;
  private isConnected = false;
  private hasBulk = false;
  private stationsCache: Map<number, Station> = new Map();
  private stationStore = useStationStore();

  private onInitialLoad?: (stations: Station[], factory: StationFactory) => void;
  private onUpdate?: (station: Station, factory: StationFactory) => void;

  constructor(factory?: StationFactory){
    this.factory = factory || new StationFactory();
  }

  connect(onInitialLoad?: (stations: Station[], factory: StationFactory)=>void, onUpdate?: (station: Station, factory: StationFactory)=>void){
    this.onInitialLoad = onInitialLoad;
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
        // Request initial load automatically
        this.requestInitialLoad();
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

    // Initial bulk
    this.client.subscribe('/topic/stations.bulk', msg => this.handleBulk(msg));
    // Individual updates
    this.client.subscribe('/topic/station.update', msg => this.handleUpdate(msg));

    console.log('[Stations WS] Subscriptions made');
  }

  private handleBulk(message: IMessage){
    try {
      const arr: StationDTO[] = JSON.parse(message.body);
      const stations: Station[] = arr.map(toStation);
      stations.forEach(st => {
        this.stationsCache.set(st.idStation, st);
        this.factory.getStationMarker(st); // updates/creates in pool
        console.log(`[Stations WS] Bulk - Station ${st.idStation} (${st.nameStation}): ⚡ ${st.electric}, ⚙️ ${st.mechanical}`);
      });
      this.hasBulk = true;
      if(this.onInitialLoad){ this.onInitialLoad(stations, this.factory); }
      console.log(`[Stations WS] Bulk received (${stations.length} stations)`);
    } catch(e){
      console.error('[Stations WS] Error parse bulk:', e, 'payload:', message.body);
    }
  }

  private handleUpdate(message: IMessage){
    try {
      const parsed = JSON.parse(message.body) as Partial<StationDTO> & { id?: number };
      const idStation = parsed.idStation ?? parsed.id;
      if (!idStation) {
        console.warn('[Stations WS] Update sin idStation, se ignora. Payload:', parsed);
        return;
      }

      // Reuse previous data to complete lat/lng when update only brings telemetry; don't require new coordinates
      const cached = this.stationsCache.get(idStation);
      const storeStation = this.stationStore?.getStationById(idStation);

      const locked = parsed.lockedPadlocks ?? cached?.lockedPadlocks ?? 0;
      const unlocked = parsed.unlockedPadlocks ?? cached?.unlockedPadlocks ?? 0;

      const merged: StationDTO = {
        idStation,
        nameStation: parsed.nameStation ?? cached?.nameStation ?? storeStation?.nameStation ?? 'Station',
        latitude: parsed.latitude ?? cached?.latitude ?? storeStation?.latitude ?? NaN,
        longitude: parsed.longitude ?? cached?.longitude ?? storeStation?.longitude ?? NaN,
        totalSlots: parsed.totalSlots ?? cached?.totalSlots ?? storeStation?.totalSlots ?? (locked + unlocked),
        availableSlots: parsed.availableSlots ?? parsed.unlockedPadlocks ?? cached?.availableSlots ?? cached?.unlockedPadlocks ?? storeStation?.availableSlots ?? unlocked,
        timestamp: parsed.timestamp ?? Date.now(),
        mechanical: parsed.mechanical ?? parsed.availableMechanicBikes ?? cached?.mechanical ?? cached?.availableMechanicBikes ?? storeStation?.mechanical,
        electric: parsed.electric ?? parsed.availableElectricBikes ?? cached?.electric ?? cached?.availableElectricBikes ?? storeStation?.electric,
        cctvStatus: parsed.cctvStatus ?? cached?.cctvStatus,
        lockedPadlocks: parsed.lockedPadlocks ?? cached?.lockedPadlocks,
        unlockedPadlocks: parsed.unlockedPadlocks ?? cached?.unlockedPadlocks,
        availableElectricBikes: parsed.availableElectricBikes ?? cached?.availableElectricBikes,
        availableMechanicBikes: parsed.availableMechanicBikes ?? cached?.availableMechanicBikes,
        slots: parsed.slots ?? undefined
      };

      // Si seguimos sin coordenadas, pero hay cache, conservar las previas; si no hay, no renderizamos pero evitamos crash
      if ((Number.isNaN(merged.latitude) || Number.isNaN(merged.longitude)) && cached) {
        merged.latitude = cached.latitude;
        merged.longitude = cached.longitude;
      }

      if (Number.isNaN(merged.latitude) || Number.isNaN(merged.longitude)) {
        // Only update telemetry cache if there is previous cache; avoid creating marker without position
        if (cached || storeStation) {
          const base = cached ?? storeStation;
          if (!base) { return; }
          const station = { ...base, ...toStation({ ...merged, latitude: base.latitude, longitude: base.longitude }) } as Station;
          this.stationsCache.set(station.idStation, station);
          if(this.onUpdate){ this.onUpdate(station, this.factory); }
          console.log(`[Stations WS] Update station ${station.idStation} (without moving marker): ⚡ ${station.electric}, ⚙️ ${station.mechanical}`);
        } else {
          console.warn('[Stations WS] Update without coordinates and without cache, cannot render or cache. idStation:', idStation);
        }
        return;
      }

      const station = toStation(merged);
      this.stationsCache.set(station.idStation, station);
      this.factory.getStationMarker(station);
      if(this.onUpdate){ this.onUpdate(station, this.factory); }
      console.log(`[Stations WS] Update station ${station.idStation} (${station.nameStation}): ⚡ ${station.electric}, ⚙️ ${station.mechanical}`);
    } catch(e){
      console.error('[Stations WS] Error parse update:', e, 'payload:', message.body);
    }
  }

  requestInitialLoad(){
    if(!this.client || !this.isConnected){
      console.warn('[Stations WS] Not connected, cannot request bulk');
      return;
    }
    this.client.publish({ destination: '/app/stations.request', body: JSON.stringify({ action: 'load_all' }) });
    console.log('[Stations WS] Bulk request sent');
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
  getHasInitialBulk(){ return this.hasBulk; }
  getFactory(){ return this.factory; }
  getStationCount(){ return this.factory.size(); }
  getStations(): Station[] { return Array.from(this.stationsCache.values()); }
  getStation(id: number): Station | undefined { return this.stationsCache.get(id); }
}
