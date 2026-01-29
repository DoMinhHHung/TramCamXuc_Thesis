package iuh.fit.se.tramcamxuc.modules.genre.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.genre.dto.request.GenreRequest;
import iuh.fit.se.tramcamxuc.modules.genre.dto.response.GenreResponse;
import iuh.fit.se.tramcamxuc.modules.genre.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllGenres() {
        return ResponseEntity.ok(ApiResponse.success(genreService.getAllGenres()));
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(
            @Valid @ModelAttribute GenreRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(genreService.createGenre(request)));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(
            @PathVariable UUID id,
            @Valid @ModelAttribute GenreRequest request) {
        return ResponseEntity.ok(ApiResponse.success(genreService.updateGenre(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteGenre(@PathVariable UUID id) {
        genreService.deleteGenre(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thể loại thành công"));
    }
}