package iuh.fit.se.tramcamxuc.modules.auth.controller;

import iuh.fit.se.tramcamxuc.common.annotation.RateLimit;
import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.auth.dto.request.*;
import iuh.fit.se.tramcamxuc.modules.auth.dto.response.AuthResponse;
import iuh.fit.se.tramcamxuc.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @RateLimit(key = "register", count = 3, period = 300)
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request)));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verify(@RequestBody Map<String, String> request) {
        authService.verifyAccount(request.get("email"), request.get("otp"));
        return ResponseEntity.ok(ApiResponse.success("Account verified successfully"));
    }

    @PostMapping("/resend-otp")
    @RateLimit(key = "resend_otp", count = 3, period = 300)
    public ResponseEntity<ApiResponse<String>> resendOtp(@RequestParam String email) {
        authService.resendOtp(email);
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestHeader("x-refresh-token") String refreshToken) {
        if (refreshToken == null) {
            throw new RuntimeException("Refresh Token is missing");
        }
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(refreshToken)));
    }

    @PostMapping("/forgot-password")
    @RateLimit(key = "forgot_password", count = 3, period = 300)
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully!"));
    }

    @PostMapping("/reset-password")
    @RateLimit(key = "reset_password", count = 5, period = 300)
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Reset password successfully!"));
    }

    @PostMapping("/resend-forgot-password-otp")
    @RateLimit(key = "resend_forgot_otp", count = 3, period = 300)
    public ResponseEntity<ApiResponse<String>> resendForgotPasswordOtp(@RequestParam String email) {
        authService.resendForgotPasswordOtp(email);
        return ResponseEntity.ok(ApiResponse.success("Reset password OTP resent successfully!"));
    }

    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<AuthResponse>> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.loginSocial(request)));
    }

    @PostMapping("/link-social")
    public ResponseEntity<ApiResponse<AuthResponse>> linkSocialAccount(@Valid @RequestBody LinkSocialAccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.linkSocialAccount(request)));
    }
}