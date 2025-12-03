package com.movilidadsostenible.viaje_service.models.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.util.Map;

@RedisHash("ReservationTemp")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationTempDTO {

    private String reservationId;
    private String userId;

    private String bicycleId;

    private Integer stationStartId;
    private String slotStartId;

    private Integer stationEndId;
    private String slotEndId;

    private String travelType;

    private long createdAt;

    public Map<String,Object> toMap() {
        return new ObjectMapper().convertValue(this, new TypeReference<>() {});
    }
}
