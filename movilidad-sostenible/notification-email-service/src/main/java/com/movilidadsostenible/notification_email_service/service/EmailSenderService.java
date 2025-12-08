package com.movilidadsostenible.notification_email_service.service;

import com.movilidadsostenible.notification_email_service.model.dto.UserDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailSenderService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  public EmailSenderService(JavaMailSender mailSender, TemplateEngine templateEngine) {
    this.mailSender = mailSender;
    this.templateEngine = templateEngine;
  }

  public void sendChargeTravelBalanceEmail(UserDTO user) {
    // Preparar contexto de la plantilla
    Context ctx = new Context();
    Map<String, Object> vars = new HashMap<>();
    vars.put("subject", safe(user.getSubject()));
    vars.put("message", safe(user.getMessage()));
    vars.put("userEmail", safe(user.getUserEmail()));

    // Extras opcionales si el mensaje trae formato conocido
    vars.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now().atOffset(ZoneOffset.UTC)));
    // Intentar inferir datos del mensaje si sigue el formato "Valor cobrado: X. Minutos excedentes: Y. Balance anterior: A. Balance nuevo: B."
    String msg = safe(user.getMessage());
    String chargeType = user.getSubject() != null && user.getSubject().toUpperCase().contains("MONTLY") ? "MONTLY" : "Balance";
    vars.put("chargeType", chargeType);
    vars.put("amount", extractInt(msg, "Valor cobrado:"));
    vars.put("excessMinutes", extractInt(msg, "Minutos excedentes:"));
    vars.put("prevBalance", extractInt(msg, "Balance anterior:"));
    vars.put("newBalance", extractInt(msg, "Balance nuevo:"));
    vars.put("year", Instant.now().atOffset(ZoneOffset.UTC).getYear());

    ctx.setVariables(vars);

    String html = templateEngine.process("chargeTravel", ctx);

    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      helper.setTo(user.getUserEmail());
      helper.setSubject(user.getSubject());
      helper.setText(html, true);
      // Adjuntar logo inline (ECORIDE.webp) desde resources/static
      try {
        helper.addInline("ecoride-logo", new ClassPathResource("static/ECORIDE.webp"));
      } catch (Exception ignored) {
        // si no se encuentra el recurso, se envía sin imagen
      }
      mailSender.send(mimeMessage);
    } catch (MessagingException e) {
      // Fallback a texto plano si falla el envío HTML
      try {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setTo(user.getUserEmail());
        helper.setSubject(user.getSubject());
        helper.setText(user.getMessage(), false);
        mailSender.send(mimeMessage);
      } catch (MessagingException ignored) {
        // Si también falla el fallback, no lanzamos excepción para no romper el flujo del consumidor
      }
    }
  }


  public void sendChargeTravelSubscriptionEmail(UserDTO user) {
    // Preparar contexto de la plantilla mensual
    Context ctx = new Context();
    Map<String, Object> vars = new HashMap<>();
    String subject = safe(user.getSubject());
    String message = safe(user.getMessage());
    String email = safe(user.getUserEmail());

    vars.put("subject", subject);
    vars.put("message", message);
    vars.put("userEmail", email);
    vars.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now().atOffset(ZoneOffset.UTC)));
    vars.put("year", Instant.now().atOffset(ZoneOffset.UTC).getYear());

    // Extraer datos del mensaje: "Viajes restantes: X." y "Minutos excedentes: Y."
    Integer travelsRemaining = extractInt(message, "Viajes restantes:");
    Integer excessMinutes = extractInt(message, "Minutos excedentes:");
    vars.put("travelsRemaining", travelsRemaining);
    vars.put("excessMinutes", excessMinutes);

    ctx.setVariables(vars);

    String html = templateEngine.process("chargeTravelMonthly", ctx);

    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      helper.setTo(email);
      helper.setSubject(subject);
      helper.setText(html, true);
      try {
        helper.addInline("ecoride-logo", new ClassPathResource("static/ECORIDE.webp"));
      } catch (Exception ignored) {}
      mailSender.send(mimeMessage);
    } catch (MessagingException e) {
      // Fallback a texto plano si falla el envío HTML
      try {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(message, false);
        mailSender.send(mimeMessage);
      } catch (MessagingException ignored) {
        // si también falla, no romper flujo
      }
    }
  }

  private String safe(String s) {
    return s == null ? "" : s;
  }

  private Integer extractInt(String text, String label) {
    try {
      int idx = text.indexOf(label);
      if (idx == -1) return null;
      String sub = text.substring(idx + label.length()).trim();
      // cortar a primer punto
      int dot = sub.indexOf('.');
      if (dot != -1) sub = sub.substring(0, dot);
      sub = sub.replaceAll("[^0-9-]", "").trim();
      if (sub.isEmpty()) return null;
      return Integer.parseInt(sub);
    } catch (Exception e) {
      return null;
    }
  }
}
