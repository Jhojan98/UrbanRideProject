package com.movilidadsostenible.viaje_service.models.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "travel")
@Data
public class Travel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "k_id_travel")
    private Integer idTravel;

    @NotNull
    @Column (name = "f_required_at")
    private LocalDateTime requiredAt;

    @NotNull
    @Column(name = "f_started_at")
    private LocalDateTime startedAt;

    @Column(name = "f_ended_at")
    private LocalDateTime endedAt;

    @NotBlank
    @Column(name = "t_status", length = 50)
    private String status;

    @NotBlank
    @Column(name = "k_uid_user")
    private String uid;

    @NotBlank
    @Column(name = "k_id_bicycle")
    private String idBicycle;

    @NotNull
    @Column(name = "k_from_id_station")
    private Integer fromIdStation;

    @Column(name = "k_to_id_station")
    private Integer toIdStation;

    @Column(name = "t_travel_type")
    @NotBlank
    private String travelType;
}
