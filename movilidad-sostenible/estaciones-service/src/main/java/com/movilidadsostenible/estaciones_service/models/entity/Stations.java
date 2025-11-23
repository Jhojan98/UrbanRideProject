package com.movilidadsostenible.estaciones_service.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "station")
public class Stations {

    @Id
    @Column(name = "k_id_station" )
    private Integer idStation;

    @Column(name = "n_station_name" )
    @NotNull
    private String stationName;

    @Column(name = "d_street")
    @NotNull
    private String street;

    @Column(name = "d_avenue")
    @NotNull
    private String avenue;

    @Column(name = "d_number")
    @NotNull
    private String number;

    @Column(name = "k_id_city")
    @NotNull
    private Integer idCity;

    // Getters and Setters

    public Integer getIdStation() {
        return idStation;
    }

    public void setIdStation(Integer idStation) {
        this.idStation = idStation;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getAvenue() {
        return avenue;
    }

    public void setAvenue(String avenue) {
        this.avenue = avenue;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getIdCity() {
        return idCity;
    }

    public void setIdCity(Integer idCity) {
        this.idCity = idCity;
    }
}
