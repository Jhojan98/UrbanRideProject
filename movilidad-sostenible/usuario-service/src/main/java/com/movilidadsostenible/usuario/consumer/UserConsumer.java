package com.movilidadsostenible.usuario.consumer;

import com.movilidadsostenible.usuario.models.dto.UserDTO;
import com.movilidadsostenible.usuario.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class UserConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserConsumer.class);

    private final UserService userService;

    public UserConsumer(UserService userService) {
        this.userService = userService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.email.to.user}")
    public void consumeJsonMessage(UserDTO userDTO) {
        LOGGER.info(String.format("JSON Message received -> %s", userDTO.toString()));

        userService.updateVerificationStatus(userDTO.getUserCc(), userDTO.isVerified());

    }
}
