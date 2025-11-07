package com.movilidadsostenible.viaje_service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public class Bicicleta {

    private Integer idBicicleta;
    private Integer serie;
    private String modelo;
    private String estadoCandado;
    private Date ultimaActualizacion;
    private double latitud;
    private double longitud;
    private double bateria;
}
