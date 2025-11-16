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
@Table(name = "users")
public class User {

    @Id
    @Column(name = "k_user_cc")
    private Integer userCc;

    @NotEmpty
    @Column(unique = true, name = "n_username")
    private String username;

    @NotBlank
    @Column(name = "n_hashed_password")
    private String hashedPassword;

    @NotBlank
    @Column(name = "n_user_first_name")
    private String firstName;

    @Column(name = "n_user_second_name")
    private String secondName;

    @NotBlank
    @Column(name = "n_user_first_lastname")
    private String firstLastname;

    @Column(name = "n_user_second_lastname")
    private String secondLastname;

    @NotNull
    @Column(name = "f_user_birthdate")
    private Date userBirthday;

    @NotEmpty
    @Column(unique = true, name = "n_user_email")
    @Email
    private String userEmail;

    @Column(name = "t_subscription_type")
    private String subscriptionType;

    @NotNull
    @Column(name = "f_user_registration_date")
    private Date userRegistrationDate;

    @NotNull
    @Column(name = "t_is_verified")
    private Boolean isVerified;

    // Getters and Setter


    public Integer getUserCc() {
        return userCc;
    }

    public void setUserCc(Integer userCc) {
        this.userCc = userCc;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getFirstLastname() {
        return firstLastname;
    }

    public void setFirstLastname(String firstLastname) {
        this.firstLastname = firstLastname;
    }

    public String getSecondLastname() {
        return secondLastname;
    }

    public void setSecondLastname(String secondLastname) {
        this.secondLastname = secondLastname;
    }

    public Date getUserBirthday() {
        return userBirthday;
    }

    public void setUserBirthday(Date userBirthday) {
        this.userBirthday = userBirthday;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public Date getUserRegistrationDate() {
        return userRegistrationDate;
    }

    public void setUserRegistrationDate(Date userRegistrationDate) {
        this.userRegistrationDate = userRegistrationDate;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

}
