package com.movilidadsostenible.viaje_service.redis;

import com.movilidadsostenible.viaje_service.clients.BicycleClient;
import com.movilidadsostenible.viaje_service.clients.SlotsClient;
import com.movilidadsostenible.viaje_service.services.ReservationTempService;
import com.movilidadsostenible.viaje_service.models.dto.ReservationTempDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    @Autowired
    private ReservationTempService reservationTempService;

    @Autowired
    private BicycleClient bicycleClient;

    @Autowired
    private SlotsClient slotsClient;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        ReservationTempDTO reservationTempDTO = reservationTempService.getExpired(expiredKey.replace(":ttl", ":data"));

        if (reservationTempDTO != null) {
            reservationTempService.releaseResources(reservationTempDTO);
            log.info("[Redis] Recursos liberados para la reserva temporal: {}", reservationTempDTO.toString());

            slotsClient.lockSlotById(reservationTempDTO.getSlotStartId());
            slotsClient.updatePadlockStatus(reservationTempDTO.getSlotEndId(), "UNLOCKED");

            reservationTempService.removeExpired(expiredKey.replace(":ttl", ":data"));

        } else {
            log.warn("[Redis] No se encontró la reserva temporal para la clave expirada: {}", expiredKey);
        }

        log.info("[Redis] Key expirada: {} (pattern={})", expiredKey, pattern == null ? null : new String(pattern));
    }
}
