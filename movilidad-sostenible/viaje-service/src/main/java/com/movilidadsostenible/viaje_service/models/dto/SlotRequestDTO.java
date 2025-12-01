package com.movilidadsostenible.viaje_service.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
public class SlotRequestDTO {
    private String idSlot;
    private String padlockStatus;
    private Integer stationId;
    private String bicycleId;
}

