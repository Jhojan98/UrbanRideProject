package com.movilidadsostenible.ciudad_service.models.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "city")
@Data
public class City {

    @Id
    @Column(name = "k_id_city")
    private Integer idCity;

    @Column(name = "n_city_name")
    @NotNull
    private String cityName;
}

