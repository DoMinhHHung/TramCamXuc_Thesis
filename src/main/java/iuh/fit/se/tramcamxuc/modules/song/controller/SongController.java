package iuh.fit.se.tramcamxuc.modules.song.controller;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.song.dto.request.UpdateSongMetadataRequest;
import iuh.fit.se.tramcamxuc.modules.song.dto.request.UploadSongRequest;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongWithAdResponse;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import iuh.fit.se.tramcamxuc.modules.song.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    // --- ARTIST API: Upload nhạc ---
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<SongResponse>> uploadSong(
            @Valid @RequestPart("data") UploadSongRequest request,
            @RequestPart("audio") MultipartFile audioFile,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile
    ) {
        return ResponseEntity.ok(ApiResponse.success(songService.uploadSong(request, audioFile, coverFile)));
    }

    @PutMapping("/{id}/metadata")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<SongResponse>> updateMetadata(
            @PathVariable UUID id,
            @Valid @RequestPart("data") UpdateSongMetadataRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile
    ) {
        return ResponseEntity.ok(ApiResponse.success(songService.updateSongMetadata(id, request, coverFile)));
    }

    @PostMapping("/{id}/request-approval")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<String>> requestApproval(@PathVariable UUID id) {
        songService.requestApproval(id);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi yêu cầu duyệt bài hát"));
    }

    @PostMapping("/{id}/toggle-visibility")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<String>> toggleVisibility(@PathVariable UUID id) {
        songService.togglePublicPrivate(id);
        return ResponseEntity.ok(ApiResponse.success("Đã thay đổi trạng thái bài hát"));
    }

    @GetMapping("/my-songs")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<Page<SongResponse>>> getMySongs(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(songService.getMySongs(pageable)));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<SongResponse>>> getAdminSongs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SongStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(songService.getAdminSongs(keyword, status, pageable)));
    }

    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> approveSong(@PathVariable UUID id) {
        songService.approveSong(id);
        return ResponseEntity.ok(ApiResponse.success("Đã duyệt bài hát! Email thông báo đã được gửi."));
    }

    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<SongResponse>>> getSongsByStatus(
            @RequestParam(defaultValue = "PENDING_APPROVAL") SongStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                songService.getSongsByStatusForAdmin(status, page, size)
        ));
    }

    @PostMapping("/{id}/listen")
    public ResponseEntity<ApiResponse<Void>> recordListen(@PathVariable UUID id) {
        songService.recordListen(id);
        return ResponseEntity.ok(ApiResponse.success("Recorded listen"));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<SongResponse>>> getTrendingSongs() {
        return ResponseEntity.ok(ApiResponse.success(songService.getTop5Trending()));
    }

    @GetMapping("/{id}/play")
    public ResponseEntity<ApiResponse<SongWithAdResponse>> getSongForPlay(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(songService.getSongWithAdInfo(id)));
    }
}