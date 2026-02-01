package iuh.fit.se.tramcamxuc.modules.admin.controller;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import iuh.fit.se.tramcamxuc.modules.song.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/songs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSongController {

    private final SongService songService;

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveSong(@PathVariable UUID id) {
        songService.approveSong(id);
        return ResponseEntity.ok(ApiResponse.success("Đã duyệt bài hát! Email thông báo đã được gửi."));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectSong(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {

        String reason = body.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            throw new AppException("Lý do từ chối không được để trống");
        }

        songService.rejectSong(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối bài hát!"));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<SongResponse>>> getSongsByStatus(
            @RequestParam(defaultValue = "PENDING_APPROVAL") SongStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                songService.getSongsByStatusForAdmin(status, page, size)
        ));
    }

}