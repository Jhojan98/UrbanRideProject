package com.movilidadsostenible.estaciones_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfigStations {

    @Value("${rabbitmq.queue.maintenance.station.cctv}")
    private String jsonQueueNameMaintenanceStationCctvPublisher;

  @Value("${rabbitmq.queue.maintenance.station.light}")
  private String jsonQueueNameMaintenanceStationLightPublisher;

  @Value("${rabbitmq.queue.station.panic.button}")
  private String jsonQueueNameStationPanicButtonPublisher;

    @Value("${rabbitmq.exchange.name}")
    public String jsonExchangeName;

    @Value("${rabbitmq.routing.maintenance.station.cctv.key}")
    public String routingJsonKeyMaintenanceStationCctvPublisher;

  @Value("${rabbitmq.routing.maintenance.station.light.key}")
  public String routingJsonKeyMaintenanceStationLightPublisher;

  @Value("${rabbitmq.routing.station.panic.button.key}")
  public String routingJsonKeyStationPanicButtonPublisher;


    // spring bean for rabbitmq json queue
    @Bean
    public Queue jsonQueueMaintenanceStationCctvConsumer() {
      return new Queue(jsonQueueNameMaintenanceStationCctvPublisher);
    }
    @Bean
    public Queue jsonQueueMaintenanceStationLightConsumer() {
      return new Queue(jsonQueueNameMaintenanceStationCctvPublisher);
    }
    @Bean
    public Queue jsonQueueStationPanicButtonConsumer() {
      return new Queue(jsonQueueNameStationPanicButtonPublisher);
    }

    // spring bean for rabbitmq jsonExchange
    @Bean
    public TopicExchange jsonExchange() {
        return new TopicExchange(jsonExchangeName);
    }

    // binding between json queue and exchange using routing key
    @Bean
    public Binding jsonBindingMaintenanceStationCctvConsumer() {
      return BindingBuilder
        .bind(jsonQueueMaintenanceStationCctvConsumer()).
        to(jsonExchange())
        .with(routingJsonKeyMaintenanceStationCctvPublisher);
    }
    @Bean
    public Binding jsonBindingMaintenanceStationLightConsumer() {
      return BindingBuilder
        .bind(jsonQueueMaintenanceStationLightConsumer()).
        to(jsonExchange())
        .with(routingJsonKeyMaintenanceStationLightPublisher);
    }
    @Bean
    public Binding jsonBindingStationPanicButtonConsumer() {
      return BindingBuilder
        .bind(jsonQueueStationPanicButtonConsumer()).
        to(jsonExchange())
        .with(routingJsonKeyStationPanicButtonPublisher);
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

