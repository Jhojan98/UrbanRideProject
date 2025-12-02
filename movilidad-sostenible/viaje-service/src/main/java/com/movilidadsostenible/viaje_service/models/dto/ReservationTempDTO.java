package com.movilidadsostenible.viaje_service.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("ReservationTemp")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationTempDTO {

    private String reservationId;
    private String userId;

    private Integer bicycleId;

    private Integer stationStartId;
    private String slotStartId;

    private Integer stationEndId;
    private String slotEndId;

    private String travelType;

    private long createdAt;

}
