package com.movilidadsostenible.estaciones_service.models.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.DynamicInsert;

@DynamicInsert
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

    @Column(name = "n_latitude")
    @NotNull
    private Double latitude;

    @Column(name = "n_length")
    @NotNull
    private Double length;

    @Column(name = "k_id_city")
    @NotNull
    private Integer idCity;

    @Column(name = "t_type")
    @NotNull
    private String type;

    @Column(name = "n_capacity")
    @NotNull
    private Integer capacity;

    @Column(name = "t_cctv_status")
    private boolean cctvStatus;

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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public boolean isCctvStatus() {
        return cctvStatus;
    }

    public void setCctvStatus(boolean cctvStatus) {
        this.cctvStatus = cctvStatus;
    }
}
