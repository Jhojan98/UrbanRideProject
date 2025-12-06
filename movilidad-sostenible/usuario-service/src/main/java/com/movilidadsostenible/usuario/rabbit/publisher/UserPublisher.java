package com.movilidadsostenible.usuario.rabbit.publisher;

import com.movilidadsostenible.usuario.models.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserPublisher {

    @Value("${rabbitmq.exchange.name}")
    private String jsonExchange;

    @Value("${rabbitmq.routing.user.to.email.key}")
    private String jsonRoutingKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserPublisher.class);

    private RabbitTemplate rabbitTemplate;

    public UserPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendJsonMessage(UserDTO userDTO) {
        LOGGER.info(String.format("JSON Message sent -> %s", userDTO.toString()));
        rabbitTemplate.convertAndSend(jsonExchange, jsonRoutingKey, userDTO);
    }
}
