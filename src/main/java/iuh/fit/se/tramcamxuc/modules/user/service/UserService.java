package iuh.fit.se.tramcamxuc.modules.user.service;

import iuh.fit.se.tramcamxuc.modules.user.dto.request.*;
import iuh.fit.se.tramcamxuc.modules.user.dto.response.*;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    User getCurrentUser();
    UserProfileResponse getCurrentUserProfile();
    UserProfileResponse updateProfile(UpdateProfileRequest request);
    CompletableFuture<String> uploadAvatar(MultipartFile file);
    void requestChangePasswordOtp();
    void changePasswordWithOtp(ChangePasswordRequest request);

//    TODO: Admin
    Page<UserProfileResponse> getAllUsers(String keyword, Role role, Pageable pageable);
    void toggleUserStatus(UUID userId);

}
