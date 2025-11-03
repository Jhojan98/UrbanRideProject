package com.movilidadsostenible.viaje_service.models.entity;

import com.movilidadsostenible.viaje_service.models.Bicicleta;
import com.movilidadsostenible.viaje_service.models.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "viaje")
public class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "k_id_viaje")
    private Integer idViaje;

    @NotNull
    @Column(name = "f_fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @NotNull
    @Column(name = "k_cedula_ciudadania_usuario")
    private Integer cedulaCiudadaniaUsuario;

    @NotNull
    @Column(name = "k_id_bicicleta")
    private Integer idBicicleta;

    @NotNull
    @Column(name = "k_serie")
    private Integer serie;

    // Getters and Setters

    public Integer getIdViaje() {
        return idViaje;
    }

    public void setIdViaje(Integer idViaje) {
        this.idViaje = idViaje;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public Integer getCedulaCiudadaniaUsuario() {
        return cedulaCiudadaniaUsuario;
    }

    public void setCedulaCiudadaniaUsuario(Integer cedulaCiudadaniaUsuario) {
        this.cedulaCiudadaniaUsuario = cedulaCiudadaniaUsuario;
    }

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
}
