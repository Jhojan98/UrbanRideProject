package com.movilidadsostenible.email_service.models.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Integer userCc;
    private String userEmail;
    private boolean verified;
}
