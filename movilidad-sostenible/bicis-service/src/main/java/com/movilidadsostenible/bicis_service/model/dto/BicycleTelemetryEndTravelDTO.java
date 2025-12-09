package com.movilidadsostenible.bicis_service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BicycleTelemetryEndTravelDTO {
    private String idBicycle;
    private Double latitude;
    private Double longitude;
    private Double battery;
    private Long timestamp;
    private Integer stationId;
}


