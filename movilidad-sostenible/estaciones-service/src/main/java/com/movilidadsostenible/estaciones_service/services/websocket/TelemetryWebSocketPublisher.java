package com.movilidadsostenible.estaciones_service.services.websocket;

import com.movilidadsostenible.estaciones_service.models.dto.StationTelemetryDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TelemetryWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public TelemetryWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendTelemetry(StationTelemetryDTO telemetry) {

        // Ejemplo de topic por bicicleta:
        String topic = "/topic/station.update";
        System.out.println("Sending telemetry to topic: " + topic);
        messagingTemplate.convertAndSend(topic, telemetry);
    }
}