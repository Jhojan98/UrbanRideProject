package com.movilidadsostenible.estaciones_service.clients;

import lombok.Data;

@Data
public class SlotRequest {
    private String slotId;
    private String padlockStatus;
    private Integer stationId;
    private String bicycleId;
}

