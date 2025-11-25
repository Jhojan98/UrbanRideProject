package com.movilidadsostenible.slots_service.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "slots")
@Data
@Schema(name = "Slot", description = "Representa un punto de anclaje (slot) en una estación")
public class Slot {

    @Id
    @Column(name = "k_id_slot", unique = true)
    @Schema(description = "Identificador del slot", example = "S-001")
    private String idSlot;

    @Column(name = "t_padlock_status")
    @Schema(description = "Estado del candado del slot", example = "LOCKED")
    private String padlockStatus;

    @Column(name = "k_id_station")
    @NotNull
    @Schema(description = "Identificador de la estación a la que pertenece el slot", example = "1")
    private Integer stationId;

    @Column(name = "k_id_bicycle")
    @Schema(description = "Identificador de la bicicleta anclada (si hay)", example = "B-123")
    private Integer bicycleId;
}
