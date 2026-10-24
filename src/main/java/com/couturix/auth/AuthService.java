package com.couturix.auth;

import com.couturix.atelier.Atelier;
import com.couturix.atelier.AtelierRepository;
import com.couturix.atelier.AtelierService;
import com.couturix.auth.dto.ChangePasswordRequest;
import com.couturix.auth.dto.ForgotPasswordRequest;
import com.couturix.auth.dto.LoginRequest;
import com.couturix.auth.dto.RefreshRequest;
import com.couturix.auth.dto.RegisterAtelierRequest;
import com.couturix.auth.dto.RegisterClientRequest;
import com.couturix.client.Client;
import com.couturix.client.ClientRepository;
import com.couturix.auth.dto.ResendOtpRequest;
import com.couturix.auth.dto.ResetPasswordOtpRequest;
import com.couturix.auth.dto.ResetPasswordRequest;
import com.couturix.auth.dto.TokenResponse;
import com.couturix.auth.dto.VerifyEmailRequest;
import com.couturix.common.audit.AuditService;
import com.couturix.common.exception.ApiException;
import com.couturix.config.CouturixProperties;
import com.couturix.abonnement.AbonnementService;
import com.couturix.domain.enums.RoleCode;
import com.couturix.security.JwtService;
import com.couturix.security.UserPrincipal;
import com.couturix.user.RoleRepository;
import com.couturix.user.User;
import com.couturix.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CouturixProperties properties;
    private final AuditService auditService;
    private final AtelierService atelierService;
    private final AtelierRepository atelierRepository;
    private final RoleRepository roleRepository;
    private final OtpService otpService;
    private final ClientRepository clientRepository;
    private final AbonnementService abonnementService;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId()).orElseThrow();
        if (!user.isEmailVerified()) {
            otpService.issue(user.getEmail(), OtpPurpose.EMAIL_VERIFY);
            throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED",
                    "Vérifiez votre e-mail avec le code OTP envoyé");
        }
        auditService.log(user.getId(), "LOGIN", "USER", user.getId(), user.getEmail());
        return issueTokens(user, principal);
    }

    @Transactional
    public TokenResponse registerAtelier(RegisterAtelierRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw ApiException.conflict("Cet e-mail est déjà utilisé");
        }
        Atelier atelier = new Atelier();
        atelier.setNom(request.atelierNom().trim());
        atelier.setSlug(atelierService.allocateSlug(request.atelierNom()));
        atelier.setRccm(request.rccm());
        atelier.setNif(request.nif());
        atelier.setSigle(request.sigle());
        atelier.setLatitude(request.latitude());
        atelier.setLongitude(request.longitude());
        atelier.setAdresse(request.adresse());
        atelier.setVille(request.ville());
        atelier.setPays(request.pays() == null || request.pays().isBlank() ? "Mali" : request.pays().trim());
        atelier.setTelephone(request.telephoneAtelier());
        atelier.setEmail(request.email());
        atelier.setDevise("XOF");
        atelier.setActif(true);
        atelier = atelierRepository.save(atelier);

        User user = new User();
        user.setEmail(request.email().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNom(request.nom().trim());
        user.setPrenom(request.prenom().trim());
        user.setTelephone(request.telephone());
        user.setActif(true);
        user.setMustChangePassword(false);
        user.setEmailVerified(false);
        if (request.plan() == null || request.plan().isBlank()) {
            throw ApiException.badRequest("Choisissez un plan");
        }
        user.setPlanCode(abonnementService.parse(request.plan()));
        user.setPlanStartedAt(Instant.now());
        user.setAtelier(atelier);
        user.setRoles(Set.of(roleRepository.findByCode(RoleCode.ADMIN)
                .orElseThrow(() -> ApiException.badRequest("Rôle ADMIN introuvable"))));
        user = userRepository.save(user);
        otpService.issue(user.getEmail(), OtpPurpose.EMAIL_VERIFY);
        auditService.log(user.getId(), "REGISTER_ATELIER", "ATELIER", atelier.getId(), atelier.getNom());
        return issueTokens(user, new UserPrincipal(user));
    }

    @Transactional
    public TokenResponse registerClient(RegisterClientRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw ApiException.conflict("Cet e-mail est déjà utilisé");
        }
        User user = new User();
        user.setEmail(request.email().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNom(request.nom().trim());
        user.setPrenom(request.prenom().trim());
        user.setTelephone(request.telephone());
        user.setActif(true);
        user.setMustChangePassword(false);
        user.setEmailVerified(false);
        user.setRoles(Set.of(roleRepository.findByCode(RoleCode.CLIENT)
                .orElseThrow(() -> ApiException.badRequest("Rôle CLIENT introuvable"))));
        user = userRepository.save(user);

        Client client = clientRepository.findFirstByTelephone(request.telephone()).orElse(null);
        if (client != null && client.getUser() != null) {
            throw ApiException.conflict("Ce numéro est déjà lié à un compte");
        }
        if (client == null) {
            client = new Client();
            client.setTelephone(request.telephone());
        }
        client.setUser(user);
        client.setNom(request.nom().trim());
        client.setPrenom(request.prenom().trim());
        client.setEmail(request.email().trim());
        client.setAdresse(request.adresse());
        client.setLatitude(request.latitude());
        client.setLongitude(request.longitude());
        clientRepository.save(client);

        otpService.issue(user.getEmail(), OtpPurpose.EMAIL_VERIFY);
        auditService.log(user.getId(), "REGISTER_CLIENT", "CLIENT", client.getId(), user.getEmail());
        return issueTokens(user, new UserPrincipal(user));
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        String hash = sha256(request.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> ApiException.unauthorized("Refresh token invalide"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.unauthorized("Refresh token expiré");
        }
        User user = stored.getUser();
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokens(user, new UserPrincipal(user));
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(sha256(refreshToken)).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            auditService.log(token.getUser().getId(), "LOGOUT", "USER", token.getUser().getId(), null);
        });
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Mot de passe actuel incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(userId);
        auditService.log(userId, "CHANGE_PASSWORD", "USER", userId, null);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.email()).ifPresent(user -> {
            otpService.issue(user.getEmail(), OtpPurpose.PASSWORD_RESET);
            auditService.log(user.getId(), "FORGOT_PASSWORD", "USER", user.getId(), null);
        });
    }

    @Transactional
    public TokenResponse verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> ApiException.badRequest("Code invalide"));
        if (user.isEmailVerified()) {
            return issueTokens(user, new UserPrincipal(user));
        }
        if (!otpService.consume(request.email(), OtpPurpose.EMAIL_VERIFY, request.code())) {
            throw ApiException.badRequest("Code OTP invalide ou expiré");
        }
        user.setEmailVerified(true);
        userRepository.save(user);
        auditService.log(user.getId(), "VERIFY_EMAIL", "USER", user.getId(), null);
        return issueTokens(user, new UserPrincipal(user));
    }

    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        OtpPurpose purpose;
        try {
            purpose = OtpPurpose.valueOf(request.purpose().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Type de code invalide");
        }
        userRepository.findByEmailIgnoreCase(request.email()).ifPresent(user -> otpService.issue(user.getEmail(), purpose));
    }

    @Transactional
    public void resetPasswordWithOtp(ResetPasswordOtpRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> ApiException.badRequest("Code invalide"));
        if (!otpService.consume(request.email(), OtpPurpose.PASSWORD_RESET, request.code())) {
            throw ApiException.badRequest("Code OTP invalide ou expiré");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        user.setEmailVerified(true);
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());
        auditService.log(user.getId(), "RESET_PASSWORD", "USER", user.getId(), null);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(sha256(request.token()))
                .orElseThrow(() -> ApiException.badRequest("Token invalide"));
        if (token.isUsed() || token.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.badRequest("Token expiré");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        token.setUsed(true);
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());
        auditService.log(user.getId(), "RESET_PASSWORD", "USER", user.getId(), null);
    }

    private TokenResponse issueTokens(User user, UserPrincipal principal) {
        String access = jwtService.generateAccessToken(principal);
        String refresh = UUID.randomUUID().toString();
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(sha256(refresh));
        entity.setExpiresAt(Instant.now().plus(properties.jwt().refreshTokenDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(entity);
        Set<String> roles = user.roleCodes().stream().map(Enum::name).collect(Collectors.toSet());
        return new TokenResponse(
                access,
                refresh,
                "Bearer",
                properties.jwt().accessTokenMinutes() * 60,
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                roles,
                user.isMustChangePassword(),
                user.isEmailVerified(),
                user.getAtelier() == null ? null : user.getAtelier().getId(),
                user.getAtelier() == null ? null : user.getAtelier().getNom(),
                user.getAtelier() == null ? null : user.getAtelier().getLogoUrl()
        );
    }

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
