package com.movilidadsostenible.notification_realtime_service.model.dto;

import lombok.Data;

@Data
public class TravelReservationDTO {
  private String message = "TRAVEL_RESERVATION";
  private String userId;
  private String reservationId;
  private String travelType;
  private Integer stationStartId;
  private Integer stationEndId;
  private String SlotStartId;

}
