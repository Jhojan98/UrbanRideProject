package com.movilidadsostenible.viaje_service.models.dto;

import lombok.Data;

@Data
public class TravelStartDTO {
  private String userId;
  private String reservationId;
  private String travelType;
  private Integer stationStartId;
  private Integer stationEndId;
  private String SlotStartId;

}
