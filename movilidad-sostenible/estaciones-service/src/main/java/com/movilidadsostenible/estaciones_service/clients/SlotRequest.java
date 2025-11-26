package com.movilidadsostenible.estaciones_service.clients;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SlotRequest {
    private String idSlot;
    private String padlockStatus;
    private Integer stationId;
    private String bicycleId;
}

