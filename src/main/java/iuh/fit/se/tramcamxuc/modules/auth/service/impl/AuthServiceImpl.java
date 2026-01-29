package iuh.fit.se.tramcamxuc.modules.auth.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.service.EmailService;
import iuh.fit.se.tramcamxuc.common.service.JwtService;
import iuh.fit.se.tramcamxuc.modules.auth.dto.request.*;
import iuh.fit.se.tramcamxuc.modules.auth.dto.response.AuthResponse;
import iuh.fit.se.tramcamxuc.modules.auth.entity.RefreshToken;
import iuh.fit.se.tramcamxuc.modules.auth.repository.RefreshTokenRepository;
import iuh.fit.se.tramcamxuc.modules.auth.service.AuthService;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.entity.enums.AuthProvider;
import iuh.fit.se.tramcamxuc.modules.user.entity.enums.Role;
import iuh.fit.se.tramcamxuc.modules.user.entity.enums.UserStatus;
import iuh.fit.se.tramcamxuc.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsServiceImpl customUserDetailsService;
    private final RestTemplate restTemplate;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Value("${application.security.otp.expiration-minutes}")
    private long otpExpirationMinutes;
    @Value("${url.to.login.with.facebook}")
    private String facebookLoginUrl;
    @Value("${url.to.login.with.google}")
    private String googleLoginUrl;

    @Value("${application.security.oauth2.google.client-id}")
    private String googleClientId;

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("Username already in use");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dateOfBirth = LocalDate.parse(request.getDob(), formatter);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .dob(dateOfBirth)
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .isActive(UserStatus.UNVERIFIED)
                .build();
        userRepository.save(user);

        sendVerificationOtp(user);

        return "Registration successful. Please check your email for OTP.";
    }

    public void verifyAccount(String email, String otp) {
        String key = "OTP:" + email;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new AppException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getIsActive() == UserStatus.ACTIVE) {
            throw new AppException("Account already verified");
        }

        user.setIsActive(UserStatus.ACTIVE);
        userRepository.save(user);

        redisTemplate.delete(key);
    }

    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getIsActive() == UserStatus.ACTIVE) {
            throw new RuntimeException("Account already verified");
        }

        sendVerificationOtp(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var userDetails = customUserDetailsService.loadUserByUsername(request.getEmail());
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        if (user.getIsActive() != UserStatus.ACTIVE) {
            throw new AppException("Account not activated. Please verify email.");
        }

        var accessToken = jwtService.generateAccessToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        saveUserRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .message("Login successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AppException("Refresh token not found"));

        if (storedToken.isRevoked()) {
            throw new AppException("Refresh token has been revoked. Security alert!");
        }

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException("Refresh token expired. Please login again.");
        }

        User user = storedToken.getUser();

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        var newAccessToken = jwtService.generateAccessToken(customUserDetailsService.loadUserByUsername(user.getEmail()));
        var newRefreshToken = jwtService.generateRefreshToken(customUserDetailsService.loadUserByUsername(user.getEmail()));

        saveUserRefreshToken(user, newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email chưa được đăng ký trong hệ thống"));

        sendForgotPasswordOtp(user);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String key = "FORGOT_PASS_OTP:" + request.getEmail();
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new AppException("OTP is invalid or has expired");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(key);
    }

    @Override
    public void resendForgotPasswordOtp(String email) {
        forgotPassword(email);
    }

    @Override
    public AuthResponse loginSocial(SocialLoginRequest request) {
        String email;
        String name;
        String avatarUrl;
        String providerId;

        if (request.getProvider() == AuthProvider.GOOGLE) {
            Map<String, Object> googleInfo = verifyGoogleToken(request.getToken());
            email = (String) googleInfo.get("email");
            name = (String) googleInfo.get("name");
            avatarUrl = (String) googleInfo.get("picture");
            providerId = (String) googleInfo.get("sub");
        } else if (request.getProvider() == AuthProvider.FACEBOOK) {
            Map<String, Object> fbInfo = verifyFacebookToken(request.getToken());
            email = (String) fbInfo.get("email");
            name = (String) fbInfo.get("name");
            Map<String, Object> pictureObj = (Map<String, Object>) fbInfo.get("picture");
            Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get("data");
            avatarUrl = (String) dataObj.get("url");
            providerId = (String) fbInfo.get("id");
        } else {
            throw new AppException("Provider is not supported");
        }

        User user = processSocialUser(email, name, avatarUrl, request.getProvider(), providerId);

        var userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        var accessToken = jwtService.generateAccessToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        saveUserRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .message("Login with " + request.getProvider() + " successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse linkSocialAccount(LinkSocialAccountRequest request) {
        String email;
        String name;
        String avatarUrl;
        String providerId;

        if (request.getProvider() == AuthProvider.GOOGLE) {
            Map<String, Object> googleInfo = verifyGoogleToken(request.getToken());
            email = (String) googleInfo.get("email");
            name = (String) googleInfo.get("name");
            avatarUrl = (String) googleInfo.get("picture");
            providerId = (String) googleInfo.get("sub");
        } else if (request.getProvider() == AuthProvider.FACEBOOK) {
            Map<String, Object> fbInfo = verifyFacebookToken(request.getToken());
            email = (String) fbInfo.get("email");
            name = (String) fbInfo.get("name");
            Map<String, Object> pictureObj = (Map<String, Object>) fbInfo.get("picture");
            Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get("data");
            avatarUrl = (String) dataObj.get("url");
            providerId = (String) fbInfo.get("id");
        } else {
            throw new AppException("Provider không được hỗ trợ");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản với email này để liên kết."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException("Old password not match. Can not link account.");
        }

        user.setProvider(request.getProvider());
        user.setProviderId(providerId);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        var userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        var accessToken = jwtService.generateAccessToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        saveUserRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .message("Link account successfully! Now you can sign in " + request.getProvider())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void sendVerificationOtp(User user) {
        String otp = String.valueOf(secureRandom.nextInt(900000) + 100000);

        redisTemplate.opsForValue().set(
                "OTP:" + user.getEmail(),
                otp,
                Duration.ofMinutes(otpExpirationMinutes)
        );

        emailService.sendHtmlEmail(
                user.getEmail(),
                "Xác thực tài khoản Trạm Cảm Xúc",
                "email/register-otp",
                Map.of("name", user.getFullName(), "otp", otp)
        );
    }

    private void sendForgotPasswordOtp(User user) {
        String otp = String.valueOf(secureRandom.nextInt(900000) + 100000);

        redisTemplate.opsForValue().set(
                "FORGOT_PASS_OTP:" + user.getEmail(),
                otp,
                Duration.ofMinutes(5)
        );

        emailService.sendHtmlEmail(
                user.getEmail(),
                "Đặt lại mật khẩu Trạm Cảm Xúc",
                "email/forgot-password",
                Map.of("name", user.getFullName(), "otp", otp)
        );
    }

    private void saveUserRefreshToken(User user, String jwtToken) {
        try {
            refreshTokenRepository.deleteByUser(user);
        } catch (Exception e) {}
        var token = RefreshToken.builder()
                .user(user)
                .token(jwtToken)
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(token);
    }

    private User processSocialUser(String email, String name, String avatarUrl, AuthProvider provider, String providerId) {
        var userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            if (!existingUser.getProvider().equals(provider)) {
                throw new AppException("This email has been register by " + existingUser.getProvider() +
                        ". Please enter old password to link account.");
            }
            existingUser.setAvatarUrl(avatarUrl);
            existingUser.setProviderId(providerId);
            return userRepository.save(existingUser);
        } else {
            String baseName = email.split("@")[0];
            String randomUsername = baseName + "_" + (new Random().nextInt(9000) + 1000);
            while (userRepository.existsByUsername(randomUsername)) {
                randomUsername = baseName + "_" + (new Random().nextInt(90000) + 10000);
            }
            String randomPassword = UUID.randomUUID().toString();

            User newUser = User.builder()
                    .email(email)
                    .username(randomUsername)
                    .fullName(name)
                    .password(passwordEncoder.encode(randomPassword))
                    .role(Role.USER)
                    .provider(provider)
                    .providerId(providerId)
                    .avatarUrl(avatarUrl)
                    .isActive(UserStatus.ACTIVE)
                    .dob(LocalDate.now())
                    .build();
            return userRepository.save(newUser);
        }
    }


    private Map<String, Object> verifyGoogleToken(String idToken) {
        try {
            String url = googleLoginUrl + idToken;
            Map<String, Object> body = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();

            if (body == null || !body.containsKey("aud")) {
                throw new AppException("Invalid Token response");
            }

            String aud = (String) body.get("aud");
            if (!aud.equals(googleClientId)) {
                throw new AppException("Token audience mismatch");
            }

            return body;
        } catch (Exception e) {
            throw new AppException("Google Token is invalid: " + e.getMessage());
        }
    }

    private Map<String, Object> verifyFacebookToken(String accessToken) {
        try {
            String url = facebookLoginUrl + accessToken;
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();
        } catch (Exception e) {
            throw new AppException("Facebook Token is invalid: " + e.getMessage());
        }
    }
}