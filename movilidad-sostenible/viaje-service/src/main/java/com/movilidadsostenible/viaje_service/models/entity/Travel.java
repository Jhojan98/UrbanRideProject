package com.movilidadsostenible.viaje_service.models.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "travel")
public class Travel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "k_id_travel")
    private Integer idTravel;

    @NotNull
    @Column(name = "f_started_at")
    private LocalDateTime startedAt;

    @Column(name = "f_ended_at")
    private LocalDateTime endedAt;

    @NotNull
    @Column(name = "t_status", length = 50)
    private String status;

    @NotNull
    @Column(name = "k_user_cc")
    private Integer userCc;

    @NotNull
    @Column(name = "k_id_bicycle")
    private Integer idBicycle;

    @NotNull
    @Column(name = "k_from_id_station")
    private Integer fromIdStation;

    @Column(name = "k_to_id_station")
    private Integer toIdStation;

    // Getters and Setters
    public Integer getIdTravel() {
        return idTravel;
    }

    public void setIdTravel(Integer idTravel) {
        this.idTravel = idTravel;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public Integer getUserCc() {
        return userCc;
    }

    public void setUserCc(Integer userCc) {
        this.userCc = userCc;
    }

    public Integer getIdBicycle() {
        return idBicycle;
    }

    public void setIdBicycle(Integer idBicycle) {
        this.idBicycle = idBicycle;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getFromIdStation() {
        return fromIdStation;
    }

    public void setFromIdStation(Integer fromIdStation) {
        this.fromIdStation = fromIdStation;
    }

    public Integer getToIdStation() {
        return toIdStation;
    }

    public void setToIdStation(Integer toIdStation) {
        this.toIdStation = toIdStation;
    }
}
