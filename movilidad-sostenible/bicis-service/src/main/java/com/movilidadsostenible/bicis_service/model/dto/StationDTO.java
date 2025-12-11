package com.movilidadsostenible.bicis_service.model.dto;

import lombok.Data;

@Data
public class StationDTO {
  private int idStation;
  private String stationName;
  private Double latitude;
  private Double length;
  private Integer idCity;
  private String type;
  private Boolean cctvStatus;
}
