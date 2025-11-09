package com.movilidadsostenible.email_service.models.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "k_id_email_verifiation")
    private Integer idEmailVerification;

    @NotNull
    @Column(name = "n_otp_hash")
    private String otpHash;

    @NotNull
    @Email
    @Column(name = "n_user_email")
    private String userEmail;

    @NotNull
    @Column(name = "f_expires_at")
    private LocalDateTime expiresAt;

    @NotNull
    @Column(name = "t_consumed")
    private boolean consumed;

    @NotNull
    @Column(name = "f_created_at")
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "k_user_cc")
    private Integer userCc;

    // Getters and Setters


    public Integer getIdEmailVerification() {
        return idEmailVerification;
    }

    public void setIdEmailVerification(Integer idEmailVerification) {
        this.idEmailVerification = idEmailVerification;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUserCc() {
        return userCc;
    }

    public void setUserCc(Integer userCc) {
        this.userCc = userCc;
    }
}
