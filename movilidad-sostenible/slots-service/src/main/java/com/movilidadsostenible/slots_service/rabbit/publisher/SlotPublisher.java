package com.movilidadsostenible.slots_service.rabbit.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SlotPublisher {

    @Value("${rabbitmq.exchange.name}")
    private String jsonExchange;

//    @Value("${rabbitmq.routing.maintenance.key}")
//    private String jsonMaintenanceRoutingKey;
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(BicisPublisher.class);
//
//    private RabbitTemplate rabbitTemplate;
//
//    public SlotPublisher(RabbitTemplate rabbitTemplate) {
//        this.rabbitTemplate = rabbitTemplate;
//    }
//
//    public void sendJsonMaintenanceMessage(BicycleTelemetryDTO bicycleTelemetryDTO) {
//      LOGGER.info(String.format("JSON Message sent -> %s", bicycleTelemetryDTO.toString()));
//      rabbitTemplate.convertAndSend(jsonExchange, jsonMaintenanceRoutingKey, bicycleTelemetryDTO);
//    }
}
