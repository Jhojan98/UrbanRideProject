package com.movilidadsostenible.viaje_service.rabbit;

import com.movilidadsostenible.viaje_service.models.dto.ReservationTempDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationPublisher {

    private final AmqpTemplate amqpTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String reservationExchange;

    @Value("${rabbitmq.routing.delay.key}")
    private String reservationDelayRoutingKey;

    public void publishReservation(ReservationTempDTO reservationTempDTO) {
        amqpTemplate.convertAndSend(
                reservationExchange,
                reservationDelayRoutingKey,
                reservationTempDTO
        );
        System.out.println("Published reservation to RabbitMQ: " + reservationTempDTO.getReservationId());
    }
}
