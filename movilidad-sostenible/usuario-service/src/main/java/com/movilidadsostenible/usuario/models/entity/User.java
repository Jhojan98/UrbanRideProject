package com.movilidadsostenible.usuario.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;


@DynamicInsert
@Entity
@Table(name = "users")
@Data
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
}
