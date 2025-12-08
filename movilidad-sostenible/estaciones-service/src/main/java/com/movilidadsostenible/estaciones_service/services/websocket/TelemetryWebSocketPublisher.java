package com.movilidadsostenible.estaciones_service.services.websocket;

import com.movilidadsostenible.estaciones_service.model.dto.StationTelemetryDTOAdmin;
import com.movilidadsostenible.estaciones_service.model.dto.StationTelemetryDTOUser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TelemetryWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public TelemetryWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendTelemetryUser(StationTelemetryDTOUser telemetry) {

      // Ejemplo de topic por bicicleta:
      String topic = "/topic/station.update/user";
      System.out.println("Sending telemetry to topic: " + topic);
      messagingTemplate.convertAndSend(topic, telemetry);
    }

    public void sendTelemetryAdmin(StationTelemetryDTOAdmin telemetry) {

      // Ejemplo de topic por bicicleta:
      String topic = "/topic/station.update/admin";
      System.out.println("Sending telemetry to topic: " + topic);
      messagingTemplate.convertAndSend(topic, telemetry);
    }
}
