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
    @Column(name = "f_request_date")
    private LocalDateTime requestDate;

    @NotNull
    @Column(name = "k_user_cc")
    private Integer userCc;

    @NotNull
    @Column(name = "k_id_bicycle")
    private Integer idBicycle;

    @NotNull
    @Column(name = "k_series")
    private Integer series;

    // Getters and Setters
    public Integer getIdTravel() {
        return idTravel;
    }

    public void setIdTravel(Integer idTravel) {
        this.idTravel = idTravel;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
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

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }
}
