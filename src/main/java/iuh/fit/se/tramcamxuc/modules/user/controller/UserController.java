package iuh.fit.se.tramcamxuc.modules.user.controller;

import iuh.fit.se.tramcamxuc.common.annotation.RateLimit;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.user.dto.request.*;
import iuh.fit.se.tramcamxuc.modules.user.dto.response.PublicProfileResponse;
import iuh.fit.se.tramcamxuc.modules.user.dto.response.UserAdminResponse;
import iuh.fit.se.tramcamxuc.modules.user.dto.response.UserProfileResponse;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMe() {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUserProfile()));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(request)));
    }

    @PostMapping("/change-password/otp")
    @RateLimit(key = "req_change_pass_otp", count = 3, period = 300)
    public ResponseEntity<ApiResponse<String>> requestChangePasswordOtp() {
        userService.requestChangePasswordOtp();
        return ResponseEntity.ok(ApiResponse.success("OTP đã được gửi đến email của bạn."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePasswordWithOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công"));
    }

    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    public CompletableFuture<ResponseEntity<ApiResponse<String>>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(file)
                .handle((url, ex) -> {
                    if (ex != null) {
                        return ResponseEntity.badRequest()
                                .body(ApiResponse.error(400, "Lỗi upload: " + ex.getMessage()));
                    }
                    return ResponseEntity.ok(ApiResponse.success(url));
                });
    }

    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<String>> onboardUser(@Valid @RequestBody OnboardingRequest request) {
        userService.onboardUser(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sở thích thành công!"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserAdminResponse>>> getUsersForAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUsersForAdmin(keyword, page, size)
        ));
    }

    @PatchMapping("/admin/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> toggleUserStatus(@PathVariable UUID id) {
        String status = userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User has been " + status));
    }
}