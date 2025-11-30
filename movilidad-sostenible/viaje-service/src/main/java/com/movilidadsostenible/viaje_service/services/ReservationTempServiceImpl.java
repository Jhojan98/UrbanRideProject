package com.movilidadsostenible.viaje_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movilidadsostenible.viaje_service.models.dto.ReservationTempDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationTempServiceImpl implements ReservationTempService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String KEY_PREFIX = "reservation_temp:";

    @Override
    public void save(ReservationTempDTO dto) {
        redisTemplate.opsForValue().set(KEY_PREFIX + dto.getReservationId(), dto);
    }

    @Override
    public ReservationTempDTO get(String reservationId) {
        Object obj = redisTemplate.opsForValue().get(KEY_PREFIX + reservationId);
        if (obj instanceof  ReservationTempDTO dto) return dto;
        return null;
    }

    @Override
    public void remove(String reservationId) {

    }

    @Override
    public void releaseResources(ReservationTempDTO dto) {

    }
}
