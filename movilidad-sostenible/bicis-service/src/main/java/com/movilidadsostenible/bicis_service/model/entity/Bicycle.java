package com.movilidadsostenible.bicis_service.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Entity
@Table(name = "bicycle")
public class Bicycle {

    @Id
    @Column(name = "k_id_bicycle")
    private Integer idBicycle;

    @NotNull
    @Column(name = "k_series")
    private Integer series;

    @NotEmpty
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

    @Column(name = "k_current_id_station")
    private Integer currentIdStation;

    // Getters and Setters

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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPadlockStatus() {
        return padlockStatus;
    }

    public void setPadlockStatus(String padlockStatus) {
        this.padlockStatus = padlockStatus;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getBattery() {
        return battery;
    }

    public void setBattery(double battery) {
        this.battery = battery;
    }

    public Integer getCurrentIdStation() {
        return currentIdStation;
    }
    public void setCurrentIdStation(Integer currentIdStation) {
        this.currentIdStation = currentIdStation;
    }
}
