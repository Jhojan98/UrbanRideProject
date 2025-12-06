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

    @Value("${rabbitmq.queue.user.to.email}")
    private String jsonQueueNameConsumer;

    @Value("${rabbitmq.exchange.name}")
    public String jsonExchangeName;

    @Value("${rabbitmq.routing.user.to.email.key}")
    public String routingJsonKeyConsumer;

    // spring bean for rabbitmq json queue
    @Bean
    public Queue jsonQueueConsumer() {
        return new Queue(jsonQueueNameConsumer);
    }
    // spring bean for rabbitmq jsonExchange
    @Bean
    public TopicExchange jsonExchange() {
        return new TopicExchange(jsonExchangeName);
    }

    // binding between json queue and exchange using routing key
    @Bean
    public Binding jsonBindingConsumer() {
        return BindingBuilder
                .bind(jsonQueueConsumer()).
                to(jsonExchange())
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

