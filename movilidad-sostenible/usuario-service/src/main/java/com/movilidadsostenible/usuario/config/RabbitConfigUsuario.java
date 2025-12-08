package com.movilidadsostenible.usuario.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfigUsuario {

    @Value("${rabbitmq.queue.user.to.email.charge.travel.balance}")
    private String jsonQueueNameChargeTravelBalancePublisher;

    @Value("${rabbitmq.queue.user.to.email.charge.travel.subscription}")
    private String jsonQueueNameChargeTravelSubscriptionPublisher;

    @Value("${rabbitmq.exchange.name}")
    public String jsonExchangeName;

    @Value("${rabbitmq.routing.user.to.email.travel.balance.key}")
    public String routingJsonKeyChargeTravelBalancePublisher;

    @Value("${rabbitmq.routing.user.to.email.travel.subscription.key}")
    public String routingJsonKeyChargeTravelSubscriptionPublisher;

    // spring bean for rabbitmq json queue
    @Bean
    public Queue jsonQueueChargeTravelBalanceConsumer() {
      return new Queue(jsonQueueNameChargeTravelBalancePublisher);
    }
    @Bean
    public Queue jsonQueueChargeTravelSubscriptionConsumer() {
      return new Queue(jsonQueueNameChargeTravelSubscriptionPublisher);
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
        .with(routingJsonKeyChargeTravelBalancePublisher);
    }
    @Bean
    public Binding jsonBindingChargeTravelSubscriptionConsumer() {
      return BindingBuilder
        .bind(jsonQueueChargeTravelSubscriptionConsumer()).
        to(jsonExchange())
        .with(routingJsonKeyChargeTravelSubscriptionPublisher);
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

