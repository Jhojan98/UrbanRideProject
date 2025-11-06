package com.movilidadsostenible.bicis_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Entity
@Table(name = "bicicleta")
public class Bicicleta {

    @Id
    @Column(name = "k_id_bicicleta")
    private Integer idBicicleta;

    @NotNull
    @Column(name = "k_serie")
    private Integer serie;

    @NotEmpty
    @Column(name = "n_modelo")
    private String modelo;

    @Column(name = "t_estado_candado")
    private String estadoCandado;

    @Column(name = "f_ultima_actualizacion")
    private Date ultimaActualizacion;

    @Column(name = "n_latitud")
    private double latitud;

    @Column(name = "n_longitud")
    private double longitud;

    @Column(name = "v_bateria")
    private double bateria;

    // Getters and Setters

    public Integer getIdBicicleta() {
        return idBicicleta;
    }

    public void setIdBicicleta(Integer idBicicleta) {
        this.idBicicleta = idBicicleta;
    }

    public Integer getSerie() {
        return serie;
    }

    public void setSerie(Integer serie) {
        this.serie = serie;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getEstadoCandado() {
        return estadoCandado;
    }

    public void setEstadoCandado(String estadoCandado) {
        this.estadoCandado = estadoCandado;
    }

    public Date getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(Date ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getBateria() {
        return bateria;
    }

    public void setBateria(double bateria) {
        this.bateria = bateria;
    }
}
