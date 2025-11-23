package com.movilidadsostenible.usuario.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "k_user_cc")
    private Integer userCc;

    @NotBlank
    @Column(unique = true, name = "k_uid_user")
    private String uidUser;

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

    @Column(name = "t_subscription_type")
    private String subscriptionType;

    @NotNull
    @Column(name = "f_user_registration_date")
    private Date userRegistrationDate;

    @NotNull
    @Column(name = "t_is_verified")
    private Boolean isVerified;

    @Column(name = "v_balance")
    private Integer balance;

    // Getters y Setters (email eliminado)

    public Integer getUserCc() { return userCc; }
    public void setUserCc(Integer userCc) { this.userCc = userCc; }

    public String getUidUser() { return uidUser; }
    public void setUidUser(String uidUser) { this.uidUser = uidUser; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getSecondName() { return secondName; }
    public void setSecondName(String secondName) { this.secondName = secondName; }

    public String getFirstLastname() { return firstLastname; }
    public void setFirstLastname(String firstLastname) { this.firstLastname = firstLastname; }

    public String getSecondLastname() { return secondLastname; }
    public void setSecondLastname(String secondLastname) { this.secondLastname = secondLastname; }

    public Date getUserBirthday() { return userBirthday; }
    public void setUserBirthday(Date userBirthday) { this.userBirthday = userBirthday; }

    public String getSubscriptionType() { return subscriptionType; }
    public void setSubscriptionType(String subscriptionType) { this.subscriptionType = subscriptionType; }

    public Date getUserRegistrationDate() { return userRegistrationDate; }
    public void setUserRegistrationDate(Date userRegistrationDate) { this.userRegistrationDate = userRegistrationDate; }

    public Boolean getIsVerified() { return isVerified; }
    public Boolean getVerified() { return isVerified; }
    public void setIsVerified(Boolean verified) { this.isVerified = verified; }
    public void setVerified(Boolean verified) { this.isVerified = verified; }

    public Integer getBalance() { return balance; }
    public void setBalance(Integer balance) { this.balance = balance; }
}
