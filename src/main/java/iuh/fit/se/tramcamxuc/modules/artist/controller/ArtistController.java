package iuh.fit.se.tramcamxuc.modules.artist.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.artist.dto.request.RegisterArtistRequest;
import iuh.fit.se.tramcamxuc.modules.artist.dto.request.UpdateArtistRequest;
import iuh.fit.se.tramcamxuc.modules.artist.dto.response.ArtistResponse;
import iuh.fit.se.tramcamxuc.modules.artist.service.ArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ArtistResponse>> register(
            @Valid @RequestBody RegisterArtistRequest request) {
        return ResponseEntity.ok(ApiResponse.success(artistService.registerArtist(request)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<ArtistResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(artistService.getMyProfile()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ApiResponse<ArtistResponse>> updateProfile(
            @Valid @RequestBody UpdateArtistRequest request) {
        return ResponseEntity.ok(ApiResponse.success(artistService.updateProfile(request)));
    }

    @GetMapping("/profile/{artistName}")
    public ResponseEntity<ApiResponse<ArtistResponse>> getPublicProfile(
            @PathVariable String artistName) {
        return ResponseEntity.ok(ApiResponse.success(artistService.getPublicProfile(artistName)));
    }

    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ARTIST')")
    public CompletableFuture<ResponseEntity<ApiResponse<ArtistResponse>>> updateAvatar(
            @RequestParam("file") MultipartFile file
    ) {
        return artistService.updateAvatar(file)
                .handle((response, ex) -> {
                    if (ex != null) {
                        return ResponseEntity.badRequest()
                                .body(ApiResponse.error(400, "Upload failed: " + ex.getMessage()));
                    }
                    return ResponseEntity.ok(ApiResponse.success(response));
                });
    }

    @PostMapping(value = "/me/cover", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ARTIST')")
    public CompletableFuture<ResponseEntity<ApiResponse<ArtistResponse>>> updateCover(
            @RequestParam("file") MultipartFile file
    ) {
        return artistService.updateCover(file)
                .handle((response, ex) -> {
                    if (ex != null) {
                        return ResponseEntity.badRequest()
                                .body(ApiResponse.error(400, "Upload failed: " + ex.getMessage()));
                    }
                    return ResponseEntity.ok(ApiResponse.success(response));
                });
    }
}