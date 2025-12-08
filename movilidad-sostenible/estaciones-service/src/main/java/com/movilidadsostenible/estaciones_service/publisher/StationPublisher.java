package com.movilidadsostenible.estaciones_service.publisher;

import com.movilidadsostenible.estaciones_service.model.dto.StationTelemetryDTOAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StationPublisher {

    @Value("${rabbitmq.exchange.name}")
    private String jsonExchange;

    @Value("${rabbitmq.routing.maintenance.station.cctv.key}")
    private String jsonMaintenanceStationCctvRoutingKey;

  @Value("${rabbitmq.routing.maintenance.station.light.key}")
  private String jsonMaintenanceStationLightRoutingKey;

  @Value("${rabbitmq.routing.station.panic.button.key}")
  private String jsonStationPanicButtonRoutingKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(StationPublisher.class);

    private RabbitTemplate rabbitTemplate;

    public StationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendJsonMaintenanceStationCctvMessage(StationTelemetryDTOAdmin stationTelemetryDTOAdmin) {
      LOGGER.info(String.format("JSON Message sent -> %s", stationTelemetryDTOAdmin.toString()));
      rabbitTemplate.convertAndSend(jsonExchange, jsonMaintenanceStationCctvRoutingKey, stationTelemetryDTOAdmin);
    }

    public void sendJsonMaintenanceStationLightMessage(StationTelemetryDTOAdmin stationTelemetryDTOAdmin) {
      LOGGER.info(String.format("JSON Message sent -> %s", stationTelemetryDTOAdmin.toString()));
      rabbitTemplate.convertAndSend(jsonExchange, jsonMaintenanceStationLightRoutingKey, stationTelemetryDTOAdmin);
    }

    public void sendJsonStationPanicButtonMessage(StationTelemetryDTOAdmin stationTelemetryDTOAdmin) {
      LOGGER.info(String.format("JSON Message sent -> %s", stationTelemetryDTOAdmin.toString()));
      rabbitTemplate.convertAndSend(jsonExchange, jsonStationPanicButtonRoutingKey, stationTelemetryDTOAdmin);
    }
}
