package iuh.fit.se.tramcamxuc.modules.album.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.album.dto.request.AddSongToAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.request.CreateAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.request.UpdateAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.response.AlbumResponse;
import iuh.fit.se.tramcamxuc.modules.album.service.AlbumService;
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
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    // --- ARTIST API ---

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<AlbumResponse>> createAlbum(
            @Valid @RequestPart("data") CreateAlbumRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile
    ) {
        return ResponseEntity.ok(ApiResponse.success(albumService.createAlbum(request, coverFile)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<AlbumResponse>> updateAlbum(
            @PathVariable UUID id,
            @Valid @RequestPart("data") UpdateAlbumRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile
    ) {
        return ResponseEntity.ok(ApiResponse.success(albumService.updateAlbum(id, request, coverFile)));
    }

    @PostMapping("/{id}/songs")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<String>> addSongs(
            @PathVariable UUID id,
            @RequestBody @Valid AddSongToAlbumRequest request
    ) {
        albumService.addSongsToAlbum(id, request);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm bài hát vào album"));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<String>> removeSong(
            @PathVariable UUID id,
            @PathVariable UUID songId
    ) {
        albumService.removeSongFromAlbum(id, songId);
        return ResponseEntity.ok(ApiResponse.success("Đã gỡ bài hát khỏi album"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<String>> deleteAlbum(@PathVariable UUID id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa album thành công"));
    }

    // API quan trọng: Gửi duyệt Album
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<String>> submitAlbum(@PathVariable UUID id) {
        albumService.submitAlbumForApproval(id);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi duyệt album! Các bài hát sẽ chuyển sang trạng thái chờ duyệt."));
    }

    @GetMapping("/my-albums")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<Page<AlbumResponse>>> getMyAlbums(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(albumService.getMyAlbums(pageable)));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumResponse>> getAlbumDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(albumService.getAlbumDetail(id)));
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<ApiResponse<Page<AlbumResponse>>> getArtistAlbums(
            @PathVariable UUID artistId,
            @PageableDefault(sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(albumService.getAlbumsByArtist(artistId, pageable)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<AlbumResponse>> getAlbumBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(albumService.getAlbumDetailBySlug(slug)));
    }
}