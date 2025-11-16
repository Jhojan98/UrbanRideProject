package com.movilidadsostenible.email_service.repositories;

import com.movilidadsostenible.email_service.models.entity.EmailVerification;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VerificationRepository extends CrudRepository<EmailVerification, Integer> {
    Optional<EmailVerification> findTopByUserCcOrderByCreatedAtDesc(Integer userCc);
}
