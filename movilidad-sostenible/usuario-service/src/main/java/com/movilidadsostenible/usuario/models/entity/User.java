package com.movilidadsostenible.usuario.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.DynamicInsert;


@DynamicInsert
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(unique = true, name = "k_uid_user")
    private String uidUser;

    @NotBlank
    @Column(name = "n_user_name")
    private String userName;

    @Column(name = "t_subscription_type")
    private String subscriptionType;

    @Column(name = "v_balance")
    private Integer balance;

    // Getters y Setters (email eliminado)
    public String getUidUser() { return uidUser; }
    public void setUidUser(String uidUser) { this.uidUser = uidUser; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getSubscriptionType() { return subscriptionType; }
    public void setSubscriptionType(String subscriptionType) { this.subscriptionType = subscriptionType; }

    public Integer getBalance() { return balance; }
    public void setBalance(Integer balance) { this.balance = balance; }
}
