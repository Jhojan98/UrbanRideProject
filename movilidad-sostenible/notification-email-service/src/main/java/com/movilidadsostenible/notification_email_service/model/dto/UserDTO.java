package com.movilidadsostenible.notification_email_service.model.dto;

import lombok.Data;

@Data
public class UserDTO {
  private String userEmail;
  private String subject;
  private String Message;
}
