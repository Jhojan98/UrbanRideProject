package com.movilidadsostenible.notification_realtime_service.model.dto;

import lombok.Data;

@Data
public class TravelEndDTO {
  private String userId;
  private Integer travelId;
  private String travelType;
  private Integer stationStartId;
  private Integer stationEndId;
  private String SlotEndId;
}
