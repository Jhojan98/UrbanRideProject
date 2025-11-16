package com.movilidadsostenible.usuario.models.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Integer userCc;
    private String userEmail;
    private boolean verified;

}
