package com.movilidadsostenible.notification_realtime_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfigRealtime {

  @Value("${rabbitmq.queue.travel.start}")
  private String jsonQueueNameTravelStartConsumer;

  @Value("${rabbitmq.queue.travel.end}")
  private String jsonQueueNameTravelEndConsumer;

  @Value("${rabbitmq.exchange.name}")
  public String jsonExchangeName;

  @Value("${rabbitmq.routing.travel.start.key}")
  public String routingJsonKeyTravelStartConsumer;

  @Value("${rabbitmq.routing.travel.end.key}")
  public String routingJsonKeyTravelEndConsumer;

  // Cola durable
  @Bean
  public Queue jsonQueueTravelStartConsumer() {
    return QueueBuilder.durable(jsonQueueNameTravelStartConsumer).build();
  }
  @Bean
  public Queue jsonQueueTravelEndConsumer() {
    return QueueBuilder.durable(jsonQueueNameTravelEndConsumer).build();
  }

  // Exchange durable
  @Bean
  public TopicExchange jsonExchange() {
    return ExchangeBuilder.topicExchange(jsonExchangeName).durable(true).build();
  }

  // Binding entre cola y exchange usando routing key
  @Bean
  public Binding jsonBindingTravelStartConsumer() {
    return BindingBuilder
      .bind(jsonQueueTravelStartConsumer())
      .to(jsonExchange())
      .with(routingJsonKeyTravelStartConsumer);
  }
  @Bean
  public Binding jsonBindingTravelEndConsumer() {
    return BindingBuilder
      .bind(jsonQueueTravelEndConsumer())
      .to(jsonExchange())
      .with(jsonQueueNameTravelEndConsumer);
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
