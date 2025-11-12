package com.movilidadsostenible.email_service.services;

import com.movilidadsostenible.email_service.models.dto.UserDTO;
import com.movilidadsostenible.email_service.models.entity.EmailVerification;
import com.movilidadsostenible.email_service.repositories.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService{

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private VerificationRepository verificationRepository;

    @Override
    public void processEmailVerification(UserDTO user) {
        // 1. Generar OTP
        String otp = otpService.generateOtp();

        // 2. Guardar OTP en la base
        EmailVerification ver = new EmailVerification();
        ver.setUserCc(user.getUserCc());
        ver.setUserEmail(user.getUserEmail());
        ver.setOtpHash(otpService.hashOtp(otp));

        ver.setCreatedAt(LocalDateTime.from(LocalDateTime.now()));
        ver.setExpiresAt(LocalDateTime.from(LocalDateTime.now().plus(10, ChronoUnit.MINUTES)));
        save(ver);

        // 3. Enviar el correo
        String verificationLink = "http://localhost:8004/verify?userCc=" + user.getUserCc() + "&otp=" + otp;

        emailSenderService.sendVerificationEmail(
                user.getUserEmail(),
                verificationLink
        );
    }

    @Override
    public boolean verifyOtp(Integer userCc, String otp) {
        // Buscar el registro de verificación por userId
        Optional<EmailVerification> opt = verificationRepository.findTopByUserCcOrderByCreatedAtDesc(userCc);
        if (opt.isEmpty()) return false;

        EmailVerification ev = opt.get();

        // Revisar expiración
        if (LocalDateTime.now().isAfter(ev.getExpiresAt())) return false;

        // Validar OTP
        boolean matches = otpService.verifyOtp(otp, ev.getOtpHash());
        if (!matches) return false;

        // Marcar como consumido
        ev.setConsumed(true);
        verificationRepository.save(ev);

        // Opcional: publicar mensaje a RabbitMQ para el microservicio Usuario
        // messagePublisher.sendVerificationSuccess(userId, ev.getEmail());

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailVerification> byId(Integer idEmailVerification) {
        return verificationRepository.findById(idEmailVerification);
    }

    @Override
    public Optional<EmailVerification> byUserCc(Integer userCc) {
        return verificationRepository.findTopByUserCcOrderByCreatedAtDesc(userCc);
    }

    @Override
    @Transactional
    public EmailVerification save(EmailVerification emailVerification) {
        return verificationRepository.save(emailVerification);
    }

    @Override
    @Transactional
    public void delete(Integer idEmailVerification) {
        verificationRepository.deleteById(idEmailVerification);
    }

    @Override
    public EmailVerification resendOtp(Integer userCc) {
        Optional<EmailVerification> currentOpt = verificationRepository.findTopByUserCcOrderByCreatedAtDesc(userCc);
        if (currentOpt.isEmpty()) {
            throw new IllegalArgumentException("No existe registro de verificación para el usuario: " + userCc);
        }
        EmailVerification current = currentOpt.get();
        // Marcar el anterior como consumido (invalidado)
        if (!current.isConsumed()) {
            current.setConsumed(true);
            verificationRepository.save(current);
        }
        // Generar nuevo OTP
        String otp = otpService.generateOtp();
        EmailVerification nuevo = new EmailVerification();
        nuevo.setUserCc(current.getUserCc());
        nuevo.setUserEmail(current.getUserEmail());
        nuevo.setOtpHash(otpService.hashOtp(otp));
        nuevo.setCreatedAt(LocalDateTime.now());
        nuevo.setExpiresAt(LocalDateTime.now().plus(10, ChronoUnit.MINUTES));
        nuevo.setConsumed(false);
        verificationRepository.save(nuevo);

        String verificationLink = "http://localhost:8004/verify?userCc=" + userCc + "&otp=" + otp;
        emailSenderService.sendVerificationEmail(current.getUserEmail(), verificationLink);
        return nuevo;
    }
}
