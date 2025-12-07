package com.movilidadsostenible.bicis_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfigBicis {

    @Value("${rabbitmq.queue.maintenance}")
    private String jsonQueueNameMaintenancePublisher;

    @Value("${rabbitmq.exchange.name}")
    public String jsonExchangeName;

    @Value("${rabbitmq.routing.maintenance.key}")
    public String routingJsonKeyMaintenancePublisher;


    // spring bean for rabbitmq json queue
    @Bean
    public Queue jsonQueueChargeTravelBalanceConsumer() {
      return new Queue(jsonQueueNameMaintenancePublisher);
    }

    // spring bean for rabbitmq jsonExchange
    @Bean
    public TopicExchange jsonExchange() {
        return new TopicExchange(jsonExchangeName);
    }

    // binding between json queue and exchange using routing key
    @Bean
    public Binding jsonBindingChargeTravelBalanceConsumer() {
      return BindingBuilder
        .bind(jsonQueueChargeTravelBalanceConsumer()).
        to(jsonExchange())
        .with(routingJsonKeyMaintenancePublisher);
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

