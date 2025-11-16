package com.movilidadsostenible.email_service.consumer;

import com.movilidadsostenible.email_service.models.dto.UserDTO;
import com.movilidadsostenible.email_service.services.EmailVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class EmailVerifiedConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerifiedConsumer.class);

    private final EmailVerificationService emailVerificationService;

    public EmailVerifiedConsumer(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.user.to.email}")
    public void consumeJsonMessage(UserDTO user) {
        LOGGER.info(String.format("JSON Message received -> %s", user.toString()));

        emailVerificationService.processEmailVerification(user);

    }
}
