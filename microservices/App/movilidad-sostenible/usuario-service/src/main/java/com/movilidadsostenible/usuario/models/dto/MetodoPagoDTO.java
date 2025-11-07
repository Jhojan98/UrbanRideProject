package com.movilidadsostenible.usuario.models.dto;

import com.movilidadsostenible.usuario.models.entity.MetodoPago;
import java.util.Date;

public class MetodoPagoDTO {
    private Long k_metodoPago;
    private String k_usuarioCC;
    private String t_tipoTarjeta;
    private String n_numeroTarjetaMasked; // Solo últimos 4 dígitos
    private String n_nombreTitular;
    private Date f_fechaExpiracion;
    private String n_marca;
    private Boolean b_principal;
    private Boolean b_activo;
    private Date f_fechaRegistro;
    private String n_direccionFacturacion;
    private String n_codigoPostal;

    // Constructor vacío
    public MetodoPagoDTO() {}

    // Constructor desde entidad (no expone número completo)
    public MetodoPagoDTO(MetodoPago metodoPago) {
        this.k_metodoPago = metodoPago.getK_metodoPago();
        this.k_usuarioCC = metodoPago.getK_usuarioCC();
        this.t_tipoTarjeta = metodoPago.getT_tipoTarjeta();
        this.n_numeroTarjetaMasked = metodoPago.getMaskedCardNumber();
        this.n_nombreTitular = metodoPago.getN_nombreTitular();
        this.f_fechaExpiracion = metodoPago.getF_fechaExpiracion();
        this.n_marca = metodoPago.getN_marca();
        this.b_principal = metodoPago.getB_principal();
        this.b_activo = metodoPago.getB_activo();
        this.f_fechaRegistro = metodoPago.getF_fechaRegistro();
        this.n_direccionFacturacion = metodoPago.getN_direccionFacturacion();
        this.n_codigoPostal = metodoPago.getN_codigoPostal();
    }

    // Getters and Setters
    public Long getK_metodoPago() {
        return k_metodoPago;
    }

    public void setK_metodoPago(Long k_metodoPago) {
        this.k_metodoPago = k_metodoPago;
    }

    public String getK_usuarioCC() {
        return k_usuarioCC;
    }

    public void setK_usuarioCC(String k_usuarioCC) {
        this.k_usuarioCC = k_usuarioCC;
    }

    public String getT_tipoTarjeta() {
        return t_tipoTarjeta;
    }

    public void setT_tipoTarjeta(String t_tipoTarjeta) {
        this.t_tipoTarjeta = t_tipoTarjeta;
    }

    public String getN_numeroTarjetaMasked() {
        return n_numeroTarjetaMasked;
    }

    public void setN_numeroTarjetaMasked(String n_numeroTarjetaMasked) {
        this.n_numeroTarjetaMasked = n_numeroTarjetaMasked;
    }

    public String getN_nombreTitular() {
        return n_nombreTitular;
    }

    public void setN_nombreTitular(String n_nombreTitular) {
        this.n_nombreTitular = n_nombreTitular;
    }

    public Date getF_fechaExpiracion() {
        return f_fechaExpiracion;
    }

    public void setF_fechaExpiracion(Date f_fechaExpiracion) {
        this.f_fechaExpiracion = f_fechaExpiracion;
    }

    public String getN_marca() {
        return n_marca;
    }

    public void setN_marca(String n_marca) {
        this.n_marca = n_marca;
    }

    public Boolean getB_principal() {
        return b_principal;
    }

    public void setB_principal(Boolean b_principal) {
        this.b_principal = b_principal;
    }

    public Boolean getB_activo() {
        return b_activo;
    }

    public void setB_activo(Boolean b_activo) {
        this.b_activo = b_activo;
    }

    public Date getF_fechaRegistro() {
        return f_fechaRegistro;
    }

    public void setF_fechaRegistro(Date f_fechaRegistro) {
        this.f_fechaRegistro = f_fechaRegistro;
    }

    public String getN_direccionFacturacion() {
        return n_direccionFacturacion;
    }

    public void setN_direccionFacturacion(String n_direccionFacturacion) {
        this.n_direccionFacturacion = n_direccionFacturacion;
    }

    public String getN_codigoPostal() {
        return n_codigoPostal;
    }

    public void setN_codigoPostal(String n_codigoPostal) {
        this.n_codigoPostal = n_codigoPostal;
    }
}
