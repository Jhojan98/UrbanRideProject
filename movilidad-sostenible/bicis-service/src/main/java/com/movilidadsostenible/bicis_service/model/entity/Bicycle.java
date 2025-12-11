package com.movilidadsostenible.bicis_service.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import java.util.Date;

@Entity
@Table(name = "bicycle")
@Data
public class Bicycle {

    @Id
    @Column(name = "k_id_bicycle")
    private String idBicycle;

    @NotNull
    @Column(name = "k_series")
    private Integer series;

    @Column(name = "n_model")
    private String model;

    @Column(name = "t_padlock_status")
    private String padlockStatus;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "f_last_update")
    private Date lastUpdate;

    @Column(name = "n_latitude")
    private double latitude;

    @Column(name = "n_length")
    private double length;

    @Column(name = "v_battery")
    private double battery;
}
