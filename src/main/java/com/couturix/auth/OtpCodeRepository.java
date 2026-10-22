package com.couturix.auth;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findTopByEmailIgnoreCaseAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            String email, OtpPurpose purpose);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByEmailIgnoreCaseAndPurpose(String email, OtpPurpose purpose);

    long countByEmailIgnoreCaseAndPurposeAndCreatedAtAfter(String email, OtpPurpose purpose, Instant after);
}
