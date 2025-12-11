package com.movilidadsostenible.viaje_service.models.dto;

import lombok.Data;

@Data
public class BicycleTelemetryEndTravelDTO {
    private String idBicycle;
    private Double latitude;
    private Double longitude;
    private Integer stationId;
    private Double battery;
}

