package com.movilidadsostenible.estaciones_service.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SlotRequestDTO {
    private String idSlot;
    private String padlockStatus;
    private Integer stationId;
    private String bicycleId;
}

