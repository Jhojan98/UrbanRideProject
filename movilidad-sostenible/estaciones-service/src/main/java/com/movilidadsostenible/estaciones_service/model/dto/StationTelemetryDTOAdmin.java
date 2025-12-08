package com.movilidadsostenible.estaciones_service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StationTelemetryDTOAdmin {
    private Integer idStation;
    private Integer availableElectricBikes;
    private Integer availableMechanicBikes;
    private boolean cctvStatus;
    private boolean panicButtonStatus;
    private boolean lightingStatus;
    private Long timestamp;
}


