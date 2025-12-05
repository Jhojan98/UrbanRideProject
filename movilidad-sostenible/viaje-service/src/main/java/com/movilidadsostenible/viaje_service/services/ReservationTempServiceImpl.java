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
    private static final String UID_PREFIX = ":user_id:";

    @Override
    public void save(ReservationTempDTO dto) {

        String baseKey = TEMP_PREFIX + dto.getReservationId();
        redisTemplate.opsForHash().putAll(baseKey+ UID_PREFIX + dto.getUserId() + DATA_SUFFIX, dto.toMap());
        redisTemplate.opsForValue().set(baseKey + UID_PREFIX + dto.getUserId() + TTL_SUFFIX, "",
                10, TimeUnit.MINUTES);
    }


  @Override
    public ReservationTempDTO getExpired(String reservationId) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(reservationId);
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        return convertHashToDto(reservationId, raw);
    }

  @Override
  public ReservationTempDTO get(String reservationId) {
      String key = TEMP_PREFIX + reservationId + DATA_SUFFIX;
      Map<Object, Object> raw = redisTemplate.opsForHash().entries(key);
      if (raw != null && !raw.isEmpty()) {
          return convertHashToDto(key, raw);
      }
      return null;
  }


  @Override
  public ReservationTempDTO getByUID(String userId) {
      // Buscar claves de datos para el usuario: reservation_temp:*:user_id:{userId}:data
      String pattern = TEMP_PREFIX + "*" + UID_PREFIX + userId + DATA_SUFFIX;
      java.util.Set<String> keys = redisTemplate.keys(pattern);
      if (keys == null || keys.isEmpty()) {
          return null;
      }
      // Tomar la primera coincidencia (si manejas múltiples reservas por usuario, aquí podrías elegir por createdAt)
      String dataKey = keys.iterator().next();

      // Verificar que el TTL aún exista; si el TTLKey no existe, consideramos expirado
      String ttlKeyBase = dataKey.substring(0, dataKey.length() - DATA_SUFFIX.length());
      String ttlKey = ttlKeyBase + TTL_SUFFIX;
      Boolean ttlExists = redisTemplate.hasKey(ttlKey);
      if (ttlExists == null || !ttlExists) {
          return null; // expirado
      }

      Map<Object, Object> raw = redisTemplate.opsForHash().entries(dataKey);
      if (raw == null || raw.isEmpty()) {
          return null;
      }
      return convertHashToDto(dataKey, raw);
  }


  @Override
    public void removeExpired(String reservationId) {
        redisTemplate.delete(reservationId);
    }

  @Override
  public void remove(String reservationId) {
    System.out.println(reservationId);
    System.out.println(reservationId.replace(":data", ":ttl"));
      redisTemplate.delete(reservationId);
      redisTemplate.delete(reservationId.replace(":data", ":ttl"));
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
