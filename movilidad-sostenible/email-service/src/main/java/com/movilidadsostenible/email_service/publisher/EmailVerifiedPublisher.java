package com.movilidadsostenible.email_service.publisher;

import com.movilidadsostenible.email_service.models.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailVerifiedPublisher {

    @Value("${rabbitmq.exchange.name}")
    private String jsonExchange;

    @Value("${rabbitmq.routing.email.to.user.key}")
    private String jsonRoutingKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerifiedPublisher.class);

    private RabbitTemplate rabbitTemplate;

    public EmailVerifiedPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendJsonMessage(UserDTO user) {
        LOGGER.info(String.format("JSON Message sent -> %s", user.toString()));
        rabbitTemplate.convertAndSend(jsonExchange, jsonRoutingKey, user);
    }
}
