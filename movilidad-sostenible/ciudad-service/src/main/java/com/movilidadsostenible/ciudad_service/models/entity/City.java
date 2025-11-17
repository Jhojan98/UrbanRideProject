package com.movilidadsostenible.ciudad_service.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "city")
public class City {

    @Id
    @Column(name = "k_id_city")
    private Integer idCity;

    @Column(name = "n_city_name")
    @NotNull
    private String cityName;

    // Getters and Setters

    public Integer getIdCity() {
        return idCity;
    }

    public void setIdCity(Integer idCity) {
        this.idCity = idCity;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
