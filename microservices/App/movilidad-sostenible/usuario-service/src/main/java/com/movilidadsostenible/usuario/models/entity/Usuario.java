package com.movilidadsostenible.usuario.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @Column(name = "k_CC")
    private String k_CC;

    private String n_primerNombre;

    private String n_segundoNombre;

    private String n_primerApellido;

    private String n_segundoApellido;

    private Date f_fechaNacimiento;

    @Column(unique = true)
    private String n_correoElectronico;

    private String t_tipoSuscripcion;

    private Date f_fechaRegistro;

    // Getters and Setters
    public String getK_CC() {
        return k_CC;
    }

    public void setK_CC(String k_CC) {
        this.k_CC = k_CC;
    }

    public String getN_primerNombre() {
        return n_primerNombre;
    }

    public void setN_primerNombre(String n_primerNombre) {
        this.n_primerNombre = n_primerNombre;
    }

    public String getN_segundoNombre() {
        return n_segundoNombre;
    }

    public void setN_segundoNombre(String n_segundoNombre) {
        this.n_segundoNombre = n_segundoNombre;
    }

    public String getN_primerApellido() {
        return n_primerApellido;
    }

    public void setN_primerApellido(String n_primerApellido) {
        this.n_primerApellido = n_primerApellido;
    }

    public String getN_segundoApellido() {
        return n_segundoApellido;
    }

    public void setN_segundoApellido(String n_segundoApellido) {
        this.n_segundoApellido = n_segundoApellido;
    }

    public Date getF_fechaNacimiento() {
        return f_fechaNacimiento;
    }

    public void setF_fechaNacimiento(Date f_fechaNacimiento) {
        this.f_fechaNacimiento = f_fechaNacimiento;
    }

    public String getN_correoElectronico() {
        return n_correoElectronico;
    }

    public void setN_correoElectronico(String n_correoElectronico) {
        this.n_correoElectronico = n_correoElectronico;
    }

    public String getT_tipoSuscripcion() {
        return t_tipoSuscripcion;
    }

    public void setT_tipoSuscripcion(String t_tipoSuscripcion) {
        this.t_tipoSuscripcion = t_tipoSuscripcion;
    }

    public Date getF_fechaRegistro() {
        return f_fechaRegistro;
    }

    public void setF_fechaRegistro(Date f_fechaRegistro) {
        this.f_fechaRegistro = f_fechaRegistro;
    }
}
