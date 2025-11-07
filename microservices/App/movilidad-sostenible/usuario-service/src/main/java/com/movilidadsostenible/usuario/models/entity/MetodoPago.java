package com.movilidadsostenible.usuario.models.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "metodo_pago")
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "k_metodoPago")
    private Long k_metodoPago;

    @Column(name = "k_usuario_cc", nullable = false)
    private String k_usuarioCC;

    @Column(name = "t_tipoTarjeta", nullable = false)
    private String t_tipoTarjeta; // CREDITO, DEBITO, PSE, EFECTIVO

    @Column(name = "n_numeroTarjeta", nullable = false)
    private String n_numeroTarjeta; // Últimos 4 dígitos enmascarados

    @Column(name = "n_numeroTarjetaCompleto")
    private String n_numeroTarjetaCompleto; // Almacenado completo (en producción debería estar encriptado)

    @Column(name = "n_nombreTitular", nullable = false)
    private String n_nombreTitular;

    @Column(name = "f_fechaExpiracion", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date f_fechaExpiracion;

    @Column(name = "n_marca")
    private String n_marca; // VISA, MASTERCARD, AMEX, etc.

    @Column(name = "b_principal")
    private Boolean b_principal = false; // Indica si es el método de pago principal

    @Column(name = "b_activo")
    private Boolean b_activo = true;

    @Column(name = "f_fechaRegistro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date f_fechaRegistro;

    @Column(name = "n_direccionFacturacion")
    private String n_direccionFacturacion;

    @Column(name = "n_codigoPostal")
    private String n_codigoPostal;

    // Constructors
    public MetodoPago() {
        this.f_fechaRegistro = new Date();
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

    public String getN_numeroTarjeta() {
        return n_numeroTarjeta;
    }

    public void setN_numeroTarjeta(String n_numeroTarjeta) {
        this.n_numeroTarjeta = n_numeroTarjeta;
    }

    public String getN_numeroTarjetaCompleto() {
        return n_numeroTarjetaCompleto;
    }

    public void setN_numeroTarjetaCompleto(String n_numeroTarjetaCompleto) {
        this.n_numeroTarjetaCompleto = n_numeroTarjetaCompleto;
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

    // Método helper para enmascarar número de tarjeta
    public String getMaskedCardNumber() {
        if (n_numeroTarjetaCompleto != null && n_numeroTarjetaCompleto.length() >= 4) {
            int length = n_numeroTarjetaCompleto.length();
            return "**** **** **** " + n_numeroTarjetaCompleto.substring(length - 4);
        }
        return n_numeroTarjeta;
    }

    // Método helper para detectar la marca de la tarjeta
    public String detectarMarca() {
        if (n_numeroTarjetaCompleto != null && !n_numeroTarjetaCompleto.isEmpty()) {
            String firstDigit = n_numeroTarjetaCompleto.substring(0, 1);
            String firstTwoDigits = n_numeroTarjetaCompleto.length() >= 2 ? 
                n_numeroTarjetaCompleto.substring(0, 2) : "";

            if (firstDigit.equals("4")) {
                return "VISA";
            } else if (firstTwoDigits.compareTo("51") >= 0 && firstTwoDigits.compareTo("55") <= 0) {
                return "MASTERCARD";
            } else if (firstTwoDigits.equals("34") || firstTwoDigits.equals("37")) {
                return "AMEX";
            } else if (firstTwoDigits.equals("36") || firstTwoDigits.equals("38") || 
                       firstTwoDigits.equals("30")) {
                return "DINERS";
            } else if (firstTwoDigits.equals("35")) {
                return "JCB";
            }
        }
        return "DESCONOCIDA";
    }
}
