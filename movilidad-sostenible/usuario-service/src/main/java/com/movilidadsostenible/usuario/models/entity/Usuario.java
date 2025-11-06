package com.movilidadsostenible.usuario.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Date;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @Column(name = "k_cedula_ciudadania_usuario")
    private Integer cedulaCiudadaniaUsuario;

    @NotEmpty
    @Column(unique = true, name = "n_usuario")
    private String usuario;

    @NotBlank
    @Column(name = "n_contrasena")
    private String contrasena;

    @NotBlank
    @Column(name = "n_primer_nombre")
    private String primerNombre;

    @Column(name = "n_segundo_nombre")
    private String segundoNombre;

    @NotBlank
    @Column(name = "n_primer_apellido")
    private String primerApellido;

    @Column(name = "n_segundo_apellido")
    private String segundoApellido;

    @NotNull
    @Column(name = "f_fecha_nacimiento")
    private Date fechaNacimiento;

    @NotEmpty
    @Column(unique = true, name = "n_correo_electronico")
    @Email
    private String correoElectronico;

    @Column(name = "t_tipo_suscripcion")
    private String tipoSuscripcion;

    @NotNull
    @Column(name = "f_fecha_de_registro")
    private Date fechaRegistro;

    // Getters and Setter


    public Integer getCedulaCiudadaniaUsuario() {
        return cedulaCiudadaniaUsuario;
    }

    public void setCedulaCiudadaniaUsuario(Integer cedulaCiudadaniaUsuario) {
        this.cedulaCiudadaniaUsuario = cedulaCiudadaniaUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getTipoSuscripcion() {
        return tipoSuscripcion;
    }

    public void setTipoSuscripcion(String tipoSuscripcion) {
        this.tipoSuscripcion = tipoSuscripcion;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
