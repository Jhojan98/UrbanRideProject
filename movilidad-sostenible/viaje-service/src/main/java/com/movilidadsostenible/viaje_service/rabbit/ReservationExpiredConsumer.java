package com.movilidadsostenible.viaje_service.rabbit;

import com.movilidadsostenible.viaje_service.models.dto.ReservationTempDTO;
import com.movilidadsostenible.viaje_service.services.ReservationTempService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiredConsumer {

    private final ReservationTempService reservationTempService;

    @RabbitListener(queues = "${rabbitmq.queue.expired}")
    public void handleExpiredReservation(ReservationTempDTO reservationTempDTO) {
        log.warn("Reservation expired: {}", reservationTempDTO.getReservationId());

        reservationTempService.remove(reservationTempDTO.getReservationId());

        try {
            reservationTempService.releaseResources(reservationTempDTO);
            log.info("Released resources for reservation ID: {}", reservationTempDTO.getReservationId());
        } catch (Exception e) {
            log.error("Error releasing resources for reservation ID: {}", reservationTempDTO.getReservationId());
        }
    }
}
