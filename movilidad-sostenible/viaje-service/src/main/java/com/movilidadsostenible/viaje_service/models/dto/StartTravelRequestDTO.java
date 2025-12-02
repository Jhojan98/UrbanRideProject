package com.movilidadsostenible.viaje_service.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StartTravelRequestDTO {
    private String userUid;
    private Integer stationStartId;
    private Integer stationEndId;
    private String bikeType;
}
