package iuh.fit.se.tramcamxuc.modules.admin.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.user.dto.response.UserAdminResponse;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/admin")
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
    public ResponseEntity<ApiResponse<String>> toggleUserStatus(@PathVariable UUID id) {
        String status = userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User has been " + status));
    }
}