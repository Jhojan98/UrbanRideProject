package com.movilidadsostenible.viaje_service.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EndTravelDTO {

  private String slotId;
  private String idBicycle;

  private Double latitudeBicycle;
  private Double longitudeBicycle;

  private Long endTravelTimestamp;

}
