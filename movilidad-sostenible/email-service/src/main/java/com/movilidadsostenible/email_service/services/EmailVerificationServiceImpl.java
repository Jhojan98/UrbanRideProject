package com.movilidadsostenible.email_service.services;

import com.movilidadsostenible.email_service.models.dto.UserDTO;
import com.movilidadsostenible.email_service.models.entity.EmailVerification;
import com.movilidadsostenible.email_service.repositories.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        ver.setOtpHash(otpService.hashOtp(otp));
        ver.setCreatedAt(LocalDateTime.now());
        ver.setExpiresAt(LocalDateTime.now().plus(10, ChronoUnit.MINUTES));
        ver.setConsumed(false);
        save(ver);

        // 3. Enviar el correo usando el email del DTO
        String verificationLink = "http://localhost:8004/verify?userCc=" + user.getUserCc() + "&otp=" + otp;
        emailSenderService.sendVerificationEmail(user.getUserEmail(), verificationLink);
    }

    @Override
    public boolean verifyOtp(Integer userCc, String otp) {
        Optional<EmailVerification> opt = verificationRepository.findTopByUserCcOrderByCreatedAtDesc(userCc);
        if (opt.isEmpty()) return false;
        EmailVerification ev = opt.get();
        if (LocalDateTime.now().isAfter(ev.getExpiresAt())) return false;
        boolean matches = otpService.verifyOtp(otp, ev.getOtpHash());
        if (!matches) return false;
        ev.setConsumed(true);
        verificationRepository.save(ev);
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
        // Invalidar OTP previo si no estaba consumido
        if (!current.isConsumed()) {
            current.setConsumed(true);
            verificationRepository.save(current);
        }
        // Generar nuevo OTP y persistir
        String otp = otpService.generateOtp();
        EmailVerification nuevo = new EmailVerification();
        nuevo.setUserCc(current.getUserCc());
        nuevo.setOtpHash(otpService.hashOtp(otp));
        nuevo.setCreatedAt(LocalDateTime.now());
        nuevo.setExpiresAt(LocalDateTime.now().plus(10, ChronoUnit.MINUTES));
        nuevo.setConsumed(false);
        verificationRepository.save(nuevo);

        // Nota: No enviamos email aquí porque la tabla no guarda el correo.
        // El reenvío debe iniciarse desde usuario-service publicando UserDTO con el email actual.
        return nuevo;
    }
}
