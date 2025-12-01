package com.movilidadsostenible.estaciones_service.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StationTelemetryDTO {
    private Integer idStation;
    private boolean cctvStatus;
    private Integer lockedPadlocks;
    private Integer unlockedPadlocks;
    private Long timestamp;
}


