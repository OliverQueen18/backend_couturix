package com.couturix.auth;

import com.couturix.config.CouturixProperties;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final OtpCodeRepository otpCodeRepository;
    private final JavaMailSender mailSender;
    private final CouturixProperties properties;

    @Transactional
    public void issue(String email, OtpPurpose purpose) {
        String normalized = email.trim();
        Instant oneMinuteAgo = Instant.now().minus(60, ChronoUnit.SECONDS);
        if (otpCodeRepository.countByEmailIgnoreCaseAndPurposeAndCreatedAtAfter(normalized, purpose, oneMinuteAgo) > 0) {
            return;
        }
        otpCodeRepository.deleteByEmailIgnoreCaseAndPurpose(normalized, purpose);
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        OtpCode otp = new OtpCode();
        otp.setEmail(normalized);
        otp.setCodeHash(AuthService.sha256(code));
        otp.setPurpose(purpose);
        otp.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        otp.setCreatedAt(Instant.now());
        otpCodeRepository.save(otp);
        sendMail(normalized, purpose, code);
        log.info("OTP {} généré pour {}", purpose, normalized);
    }

    @Transactional
    public boolean consume(String email, OtpPurpose purpose, String code) {
        if (email == null || code == null || code.isBlank()) {
            return false;
        }
        return otpCodeRepository
                .findTopByEmailIgnoreCaseAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(email.trim(), purpose)
                .filter(otp -> !otp.getExpiresAt().isBefore(Instant.now()))
                .filter(otp -> otp.getCodeHash().equals(AuthService.sha256(code.trim())))
                .map(otp -> {
                    otp.setConsumedAt(Instant.now());
                    otpCodeRepository.save(otp);
                    return true;
                })
                .orElse(false);
    }

    private void sendMail(String email, OtpPurpose purpose, String code) {
        String subject = purpose == OtpPurpose.PASSWORD_RESET
                ? "CouturiX — code de réinitialisation"
                : "CouturiX — vérification de votre e-mail";
        String body = purpose == OtpPurpose.PASSWORD_RESET
                ? "Votre code pour réinitialiser le mot de passe est : " + code + "\nIl expire dans 10 minutes."
                : "Votre code de vérification CouturiX est : " + code + "\nIl expire dans 10 minutes.";
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(properties.notifications().fromEmail());
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Envoi OTP impossible vers {}: {}", email, ex.getMessage());
        }
    }
}
