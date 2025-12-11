package com.movilidadsostenible.notification_realtime_service.model.dto;

import lombok.Data;

@Data
public class TravelStartDTO {
  private String message = "TRAVEL_START";
  private String userId;
  private String travelId;
  private String travelType;
  private Integer stationStartId;
  private Integer stationEndId;
  private String SlotEndId;
}
