package com.movilidadsostenible.notification_email_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfigEmail {

  // Permitir override por variables de entorno en Docker Compose
  @Value("${rabbitmq.queue.user.to.email:${RABBITMQ_QUEUE_USER_TO_EMAIL:user.to.email.queue}}")
  private String jsonQueueNameConsumer;

  @Value("${rabbitmq.exchange.name:${RABBITMQ_EXCHANGE_NAME:app.topic.exchange}}")
  private String jsonExchangeName;

  @Value("${rabbitmq.routing.user.to.email.key:${RABBITMQ_ROUTING_USER_TO_EMAIL:user.to.email.key}}")
  private String routingJsonKeyConsumer;

  // Cola durable
  @Bean
  public Queue jsonQueueConsumer() {
    return QueueBuilder.durable(jsonQueueNameConsumer).build();
  }

  // Exchange durable
  @Bean
  public TopicExchange jsonExchange() {
    return ExchangeBuilder.topicExchange(jsonExchangeName).durable(true).build();
  }

  // Binding entre cola y exchange usando routing key
  @Bean
  public Binding jsonBindingConsumer() {
    return BindingBuilder
      .bind(jsonQueueConsumer())
      .to(jsonExchange())
      .with(routingJsonKeyConsumer);
  }

  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(messageConverter());
    return rabbitTemplate;
  }
}
