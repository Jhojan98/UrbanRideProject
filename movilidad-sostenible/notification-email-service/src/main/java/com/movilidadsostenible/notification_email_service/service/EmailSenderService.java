package com.movilidadsostenible.notification_email_service.service;

import com.movilidadsostenible.notification_email_service.model.dto.UserDTO;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

  private final JavaMailSender mailSender;

  public EmailSenderService (JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendEmail(UserDTO user) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(user.getUserEmail());
    msg.setSubject(user.getSubject());
    msg.setText(user.getMessage());

    mailSender.send(msg);
  }
}
