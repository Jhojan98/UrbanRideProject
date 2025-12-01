package com.movilidadsostenible.viaje_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitConfigStation {

    // Tiempo de expiración por defecto: 10 minutos
    public static final int TTL_10_MIN = 600_000;

    // --- Reservation (TTL + DLQ) properties ---
    @Value("${rabbitmq.exchange.name}")
    private String reservationExchange;

    @Value("${rabbitmq.queue.delay}")
    private String reservationDelayQueue;

    @Value("${rabbitmq.queue.expired}")
    private String reservationExpiredQueue;

    @Value("${rabbitmq.routing.delay.key}")
    private String reservationDelayRoutingKey;

    @Value("${rabbitmq.routing.expired.key}")
    private String reservationExpiredRoutingKey;

    // ----- Reservation -----
    @Bean
    public TopicExchange reservationTopicExchange() {
        return new TopicExchange(reservationExchange);
    }

    // Delay queue: mensajes aquí expirarán (x-message-ttl) y serán reenviados al DLX
    @Bean
    public Queue reservationDelayQueue() {
        Map<String, Object> args = Map.of(
                "x-message-ttl", TTL_10_MIN,
                "x-dead-letter-exchange", reservationExchange,
                "x-dead-letter-routing-key", reservationExpiredRoutingKey
        );
        return new Queue(reservationDelayQueue, true, false, false, args);
    }

    // DLQ donde llegan los mensajes expirados
    @Bean
    public Queue reservationExpiredQueue() {
        return new Queue(reservationExpiredQueue, true);
    }

    @Bean
    public Binding reservationDelayBinding() {
        return BindingBuilder
                .bind(reservationDelayQueue())
                .to(reservationTopicExchange())
                .with(reservationDelayRoutingKey);
    }

    @Bean
    public Binding reservationExpiredBinding() {
        return BindingBuilder
                .bind(reservationExpiredQueue())
                .to(reservationTopicExchange())
                .with(reservationExpiredRoutingKey);
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
