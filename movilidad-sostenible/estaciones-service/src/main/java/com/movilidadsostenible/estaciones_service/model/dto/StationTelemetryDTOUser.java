package com.movilidadsostenible.estaciones_service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StationTelemetryDTOUser {
    private Integer idStation;
    private Integer availableElectricBikes;
    private Integer availableMechanicBikes;
    private Long timestamp;
}


