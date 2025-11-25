package com.movilidadsostenible.bicis_service.services.websocket;

import com.movilidadsostenible.bicis_service.model.dto.BicycleTelemetryDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TelemetryWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public TelemetryWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendTelemetry(BicycleTelemetryDTO telemetry) {

        // Ejemplo de topic por bicicleta:
        String topic = "/topic/bicycle.location";
        System.out.println("Sending telemetry to topic: " + topic);
        messagingTemplate.convertAndSend(topic, telemetry);
    }
}