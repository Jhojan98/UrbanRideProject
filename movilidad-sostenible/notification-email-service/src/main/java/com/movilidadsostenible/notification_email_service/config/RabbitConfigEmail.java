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

  @Value("${rabbitmq.queue.user.to.email.charge.travel.balance}")
  private String jsonQueueNameChargeTravelBalanceConsumer;

  @Value("${rabbitmq.queue.user.to.email.charge.travel.subscription}")
  private String jsonQueueNameChargeTravelSubscriptionConsumer;

  @Value("${rabbitmq.exchange.name}")
  public String jsonExchangeName;

  @Value("${rabbitmq.routing.user.to.email.travel.balance.key}")
  public String routingJsonKeyChargeTravelBalanceConsumer;

  @Value("${rabbitmq.routing.user.to.email.travel.subscription.key}")
  public String routingJsonKeyChargeTravelSubscriptionConsumer;

  // Cola durable
  @Bean
  public Queue jsonQueueChargeTravelBalanceConsumer() {
    return QueueBuilder.durable(jsonQueueNameChargeTravelBalanceConsumer).build();
  }
  @Bean
  public Queue jsonQueueChargeTravelSubscriptionConsumer() {
    return QueueBuilder.durable(jsonQueueNameChargeTravelSubscriptionConsumer).build();
  }

  // Exchange durable
  @Bean
  public TopicExchange jsonExchange() {
    return ExchangeBuilder.topicExchange(jsonExchangeName).durable(true).build();
  }

  // Binding entre cola y exchange usando routing key
  @Bean
  public Binding jsonBindingChargeTravelBalanceConsumer() {
    return BindingBuilder
      .bind(jsonQueueChargeTravelBalanceConsumer())
      .to(jsonExchange())
      .with(routingJsonKeyChargeTravelBalanceConsumer);
  }
  @Bean
  public Binding jsonBindingChargeTravelSubscriptionConsumer() {
    return BindingBuilder
      .bind(jsonQueueChargeTravelSubscriptionConsumer())
      .to(jsonExchange())
      .with(jsonQueueNameChargeTravelSubscriptionConsumer);
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
