import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { type BicycleDTO, toBicycle } from '@/models/Bicycle';
import { type BicycleFactory } from '@/patterns/BicycleFlyweight';

/**
 * BicycleWebSocketService - Servicio para gestionar la conexión WebSocket
 * y la suscripción al tópico de bicicletas
 */
export class BicycleWebSocketService {
    private client: Client | null = null;
    private bicycleFactory: BicycleFactory;
    private onBicycleUpdate?: (factory: BicycleFactory) => void;
    private isConnected = false;

    constructor(bicycleFactory: BicycleFactory) {
        this.bicycleFactory = bicycleFactory;
    }

    /**
     * Conecta al WebSocket y se suscribe al tópico de bicicletas
     */
    public connect(onUpdate?: (factory: BicycleFactory) => void): void {
        this.onBicycleUpdate = onUpdate;

        // Conexión directa al microservicio de bicis (no vía gateway)
        // SockJS requiere http/https, NO ws/wss (maneja la actualización automáticamente)
        const baseUrl = process.env.VUE_APP_WEBSOCKET_BICYCLES_URL || 'http://localhost:8002';
        const wsUrl = `${baseUrl}/ws`;
        
        console.log('🔌 Conectando a WebSocket:', wsUrl);

        this.client = new Client({
            // Usar SockJS como transporte
            webSocketFactory: () => new SockJS(wsUrl) as WebSocket,
            
            debug: (str) => {
                console.log('STOMP Debug:', str);
            },

            reconnectDelay: 5000, // Reconectar cada 5 segundos si se pierde la conexión
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,

            onConnect: () => {
                console.log('✅ WebSocket conectado exitosamente');
                this.isConnected = true;
                this.subscribeToTopic();
            },

            onStompError: (frame) => {
                console.error('❌ Error STOMP:', frame.headers['message']);
                console.error('Detalles:', frame.body);
                this.isConnected = false;
            },

            onWebSocketClose: () => {
                console.warn('⚠️ WebSocket cerrado');
                this.isConnected = false;
            },

            onWebSocketError: (error) => {
                console.error('❌ Error en WebSocket:', error);
                this.isConnected = false;
            }
        });

        // Activar la conexión
        this.client.activate();
    }

    /**
     * Se suscribe al tópico de ubicación de bicicletas
     */
    private subscribeToTopic(): void {
        if (!this.client) {
            console.error('Cliente STOMP no inicializado');
            return;
        }

        const topic = '/topic/bicycle.location';
        console.log('📡 Suscribiéndose al tópico:', topic);

        this.client.subscribe(topic, (message) => {
            try {
                // Parsear el mensaje recibido
                const bicycleDTO: BicycleDTO = JSON.parse(message.body);
                
                console.log('📦 Bicicleta recibida:', {
                    id: bicycleDTO.idBicycle,
                    lat: bicycleDTO.latitude,
                    lon: bicycleDTO.longitude,
                    battery: bicycleDTO.battery,
                    timestamp: new Date(bicycleDTO.timestamp).toLocaleString('es-CO')
                });

                // Convertir DTO a modelo Bicycle
                const bicycle = toBicycle(bicycleDTO);

                // Usar el Factory para obtener/actualizar el marcador
                this.bicycleFactory.getBicycleMarker(bicycle);

                // Notificar a los listeners que hay una actualización
                if (this.onBicycleUpdate) {
                    this.onBicycleUpdate(this.bicycleFactory);
                }

            } catch (error) {
                console.error('Error al procesar mensaje del WebSocket:', error);
                console.error('Mensaje recibido:', message.body);
            }
        });

        console.log('✅ Suscripción al tópico exitosa');
    }

    /**
     * Desconecta el WebSocket
     */
    public disconnect(): void {
        if (this.client && this.isConnected) {
            console.log('🔌 Desconectando WebSocket...');
            this.client.deactivate();
            this.isConnected = false;
        }
    }

    /**
     * Verifica si está conectado
     */
    public getIsConnected(): boolean {
        return this.isConnected;
    }

    /**
     * Obtiene la cantidad de bicicletas en el pool
     */
    public getBicycleCount(): number {
        return this.bicycleFactory.size();
    }
}
