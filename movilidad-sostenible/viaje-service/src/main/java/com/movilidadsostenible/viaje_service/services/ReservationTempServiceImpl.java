package com.movilidadsostenible.viaje_service.services;

import com.movilidadsostenible.viaje_service.models.dto.ReservationTempDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationTempServiceImpl implements ReservationTempService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TEMP_PREFIX = "reservation_temp:";
    private static final String DATA_SUFFIX = ":data";
    private static final String TTL_SUFFIX  = ":ttl";

    @Override
    public void save(ReservationTempDTO dto) {

        String baseKey = TEMP_PREFIX + dto.getReservationId();

        // 1️⃣ Guardar los datos SIN TTL
        redisTemplate.opsForHash().putAll(baseKey + DATA_SUFFIX, dto.toMap());

        // 2️⃣ Crear la clave que expira SOLO para disparar el evento
        redisTemplate.opsForValue().set(baseKey + TTL_SUFFIX, "",
                10, TimeUnit.SECONDS);
    }


    @Override
    public ReservationTempDTO get(String reservationId) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(reservationId);

        if (raw == null || raw.isEmpty()) {
            return null;
        }

        return convertHashToDto(reservationId, raw);
    }

    @Override
    public void remove(String reservationId) {
        redisTemplate.delete(reservationId);
    }

    @Override
    public void releaseResources(ReservationTempDTO dto) {

    }

    private ReservationTempDTO convertHashToDto(String key, Map<Object, Object> map) {
        ReservationTempDTO dto = new ReservationTempDTO();

        dto.setReservationId(key);
        dto.setUserId( String.valueOf(map.get("userId")) );
        dto.setBicycleId( String.valueOf(map.get("bicycleId")) );
        dto.setStationStartId( parseInt(map.get("stationStartId")) );
        dto.setSlotStartId( String.valueOf(map.get("slotStartId")) );
        dto.setStationEndId( parseInt(map.get("stationEndId")) );
        dto.setSlotEndId( String.valueOf(map.get("slotEndId")) );
        dto.setTravelType( String.valueOf(map.get("travelType")) );
        dto.setCreatedAt( parseLong(map.get("createdAt")) );

        return dto;
    }

    private Integer parseInt(Object value) {
        if (value == null) return null;
        return Integer.valueOf(String.valueOf(value));
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        return Long.valueOf(String.valueOf(value));
    }



}
