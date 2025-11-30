package com.movilidadsostenible.viaje_service.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
