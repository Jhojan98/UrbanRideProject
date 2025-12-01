package com.movilidadsostenible.admin.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Entity
@Table(name = "admins")
@Data
public class Admin {

    @Id
    @Column(unique = true, name = "k_uid_admin")
    private String uidUser;

    @NotBlank
    @Column(name = "n_admin_name")
    private String userName;
}
