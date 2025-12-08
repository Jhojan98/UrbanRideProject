package com.movilidadsostenible.notification_realtime_service.model.dto;

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
