package iuh.fit.se.tramcamxuc.modules.admin.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.album.dto.response.AlbumResponse;
import iuh.fit.se.tramcamxuc.modules.album.repository.AlbumRepository;
import iuh.fit.se.tramcamxuc.modules.album.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/albums")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAlbumController {

    private final AlbumService albumService;
    private final AlbumRepository albumRepository;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<AlbumResponse>>> getAllAlbumsForAdmin(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 12) Pageable pageable
    ) {
        Page<AlbumResponse> albums;
        if (keyword != null && !keyword.trim().isEmpty()) {
            albums = albumRepository.searchAlbumsForAdmin(keyword, pageable)
                    .map(AlbumResponse::fromEntity);
        } else {
            albums = albumRepository.findAllWithArtist(pageable)
                    .map(AlbumResponse::fromEntity);
        }
        return ResponseEntity.ok(ApiResponse.success(albums));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveAlbum(@PathVariable UUID id) {
        albumService.approveAlbum(id);
        return ResponseEntity.ok(ApiResponse.success("Duyệt Album thành công!"));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectAlbum(
            @PathVariable UUID id,
            @RequestBody String reason
    ) {
        albumService.rejectAlbum(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối Album."));
    }
}