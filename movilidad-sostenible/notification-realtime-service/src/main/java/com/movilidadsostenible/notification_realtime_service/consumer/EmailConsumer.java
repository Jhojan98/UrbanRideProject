package com.movilidadsostenible.notification_realtime_service.consumer;

import com.movilidadsostenible.notification_realtime_service.controllers.SseController;
import com.movilidadsostenible.notification_realtime_service.model.dto.TravelEndDTO;
import com.movilidadsostenible.notification_realtime_service.model.dto.TravelStartDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

  @Autowired
  private SseController sseController;

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailConsumer.class);

  @RabbitListener(queues = "${rabbitmq.queue.travel.start}")
  public void consumeJsonTravelStartMessage(TravelStartDTO travelStartDTO) {
    LOGGER.info("JSON Message received -> {}", travelStartDTO);
    sseController.sendToUser(travelStartDTO.getUserId(), travelStartDTO);
  }

  @RabbitListener(queues = "${rabbitmq.queue.travel.end}")
  public void consumeJsonTravelEndMessage(TravelEndDTO travelEndDTO) {
    LOGGER.info("JSON Message received -> {}", travelEndDTO);
    sseController.sendToUser(travelEndDTO.getUserId(), travelEndDTO);
  }
}
