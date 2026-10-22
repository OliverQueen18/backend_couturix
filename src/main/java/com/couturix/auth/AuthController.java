package com.couturix.auth;

import com.couturix.auth.dto.ChangePasswordRequest;
import com.couturix.auth.dto.ForgotPasswordRequest;
import com.couturix.auth.dto.LoginRequest;
import com.couturix.auth.dto.RefreshRequest;
import com.couturix.auth.dto.RegisterAtelierRequest;
import com.couturix.auth.dto.RegisterClientRequest;
import com.couturix.auth.dto.ResendOtpRequest;
import com.couturix.auth.dto.ResetPasswordOtpRequest;
import com.couturix.auth.dto.ResetPasswordRequest;
import com.couturix.auth.dto.TokenResponse;
import com.couturix.auth.dto.VerifyEmailRequest;
import com.couturix.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register-atelier")
    public TokenResponse registerAtelier(@Valid @RequestBody RegisterAtelierRequest request) {
        return authService.registerAtelier(request);
    }

    @PostMapping("/register-client")
    public TokenResponse registerClient(@Valid @RequestBody RegisterClientRequest request) {
        return authService.registerClient(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestBody(required = false) RefreshRequest request) {
        authService.logout(request == null ? null : request.refreshToken());
    }

    @PostMapping("/change-password")
    public void changePassword(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request);
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
    }

    @PostMapping("/verify-email")
    public TokenResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return authService.verifyEmail(request);
    }

    @PostMapping("/resend-otp")
    public void resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request);
    }

    @PostMapping("/reset-password-otp")
    public void resetPasswordOtp(@Valid @RequestBody ResetPasswordOtpRequest request) {
        authService.resetPasswordWithOtp(request);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }
}
