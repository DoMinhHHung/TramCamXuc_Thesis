package iuh.fit.se.tramcamxuc.modules.playlist.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.AddSongRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.CreatePlaylistRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.ReorderPlaylistRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.UpdatePlaylistRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.response.PlaylistResponse;
import iuh.fit.se.tramcamxuc.modules.playlist.service.PlaylistService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PlaylistResponse>> createPlaylist(
            @Valid @RequestPart("data") CreatePlaylistRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.createPlaylist(request, thumbnail)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PlaylistResponse>> updatePlaylist(
            @PathVariable UUID id,
            @Valid @RequestPart("data") UpdatePlaylistRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.updatePlaylist(id, request, thumbnail)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePlaylist(@PathVariable UUID id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa playlist thành công"));
    }

    @PostMapping("/{id}/songs")
    public ResponseEntity<ApiResponse<String>> addSong(
            @PathVariable UUID id,
            @RequestBody @Valid AddSongRequest request
    ) {
        playlistService.addSongToPlaylist(id, request);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm bài hát vào playlist"));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<ApiResponse<String>> removeSong(
            @PathVariable UUID id,
            @PathVariable UUID songId
    ) {
        playlistService.removeSongFromPlaylist(id, songId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa bài hát khỏi playlist"));
    }

    @GetMapping("/my-playlists")
    public ResponseEntity<ApiResponse<Page<PlaylistResponse>>> getMyPlaylists(
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getMyPlaylists(pageable)));
    }

    @PutMapping("/{id}/songs/reorder")
    public ResponseEntity<ApiResponse<String>> reorderSongs(
            @PathVariable UUID id,
            @Valid @RequestBody ReorderPlaylistRequest request
    ) {
        playlistService.reorderPlaylist(id, request);

        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật thứ tự playlist"));
    }

    // --- PUBLIC ENDPOINTS ---

    @GetMapping("/explore")
    public ResponseEntity<ApiResponse<Page<PlaylistResponse>>> getPublicPlaylists(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getPublicPlaylists(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> getPlaylistDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getPlaylistDetail(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> getPlaylistBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getPlaylistBySlug(slug)));
    }
}