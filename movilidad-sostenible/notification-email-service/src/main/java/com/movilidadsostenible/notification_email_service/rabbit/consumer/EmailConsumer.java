package com.movilidadsostenible.notification_email_service.rabbit.consumer;

import com.movilidadsostenible.notification_email_service.model.dto.UserDTO;
import com.movilidadsostenible.notification_email_service.service.EmailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailConsumer.class);

  private final EmailSenderService emailSenderService;

  public EmailConsumer(EmailSenderService emailSenderService) {
    this.emailSenderService = emailSenderService;
  }

  @RabbitListener(queues = "${rabbitmq.queue.user.to.email}")
  public void consumeJsonMessage(UserDTO user) {
    LOGGER.info("JSON Message received -> {}", user);
    emailSenderService.sendEmail(user);
  }
}
