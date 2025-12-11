package com.movilidadsostenible.slots_service.model.dto;

import lombok.Data;

@Data
public class SlotDTO {
    private String idSlot;
    private String padlockStatus;
    private Double latitude;
    private Double longitude;
    private Long timestamp;
}
