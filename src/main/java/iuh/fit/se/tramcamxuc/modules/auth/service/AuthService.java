package iuh.fit.se.tramcamxuc.modules.auth.service;

import iuh.fit.se.tramcamxuc.modules.auth.dto.request.*;
import iuh.fit.se.tramcamxuc.modules.auth.dto.response.AuthResponse;

public interface AuthService {
    String register(RegisterRequest request);
    void verifyAccount(String email, String otp);
    void resendOtp(String email);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequest request);
    void resendForgotPasswordOtp(String email);

//    Social Login
    AuthResponse loginSocial(SocialLoginRequest request);
    AuthResponse linkSocialAccount(LinkSocialAccountRequest request);

}
