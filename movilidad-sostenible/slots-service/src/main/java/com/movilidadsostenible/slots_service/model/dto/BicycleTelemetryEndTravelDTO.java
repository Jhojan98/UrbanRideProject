package com.movilidadsostenible.slots_service.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BicycleTelemetryEndTravelDTO {
    private String idBicycle;
    private Double latitude;
    private Double longitude;
    private Integer stationId;
    private Double battery;
}

