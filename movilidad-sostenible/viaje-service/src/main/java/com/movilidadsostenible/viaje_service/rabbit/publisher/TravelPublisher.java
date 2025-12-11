package com.movilidadsostenible.viaje_service.rabbit.publisher;

import com.movilidadsostenible.viaje_service.models.dto.TravelEndDTO;
import com.movilidadsostenible.viaje_service.models.dto.TravelStartDTO;
import com.movilidadsostenible.viaje_service.models.dto.TravelReservationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TravelPublisher {

    @Value("${rabbitmq.exchange.name}")
    private String jsonExchange;

  @Value("${rabbitmq.routing.travel.reservation.key}")
  private String jsonTravelReservationRoutingKey;

  @Value("${rabbitmq.routing.travel.start.key}")
  private String jsonTravelStartRoutingKey;

    @Value("${rabbitmq.routing.travel.end.key}")
    private String jsonTravelEndRoutingKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(TravelPublisher.class);

    private RabbitTemplate rabbitTemplate;

    public TravelPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

  public void sendJsonTravelReservationMessage(TravelReservationDTO travelStartDTO) {
    LOGGER.info(String.format("JSON Message sent -> %s", travelStartDTO.toString()));
    rabbitTemplate.convertAndSend(jsonExchange, jsonTravelReservationRoutingKey, travelStartDTO);
  }
  public void sendJsonTravelStartMessage(TravelStartDTO travelStartDTO) {
    LOGGER.info(String.format("JSON Message sent -> %s", travelStartDTO.toString()));
    rabbitTemplate.convertAndSend(jsonExchange, jsonTravelStartRoutingKey, travelStartDTO);
  }
  public void sendJsonTravelEndMessage(TravelEndDTO travelEndDTO) {
    LOGGER.info(String.format("JSON Message sent -> %s", travelEndDTO.toString()));
    rabbitTemplate.convertAndSend(jsonExchange, jsonTravelEndRoutingKey, travelEndDTO);
  }
}
