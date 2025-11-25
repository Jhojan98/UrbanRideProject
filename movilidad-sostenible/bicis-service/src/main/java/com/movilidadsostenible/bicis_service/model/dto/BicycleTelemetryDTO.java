package com.movilidadsostenible.bicis_service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BicycleTelemetryDTO {
    private Integer idBicycle;
    private Double latitude;
    private Double longitude;
    private Double battery;
    private Long timestamp;

    public Integer getIdBicycle() {
        return idBicycle;
    }

    public void setIdBicycle(Integer idBicycle) {
        this.idBicycle = idBicycle;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getBattery() {
        return battery;
    }

    public void setBattery(Double battery) {
        this.battery = battery;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}


