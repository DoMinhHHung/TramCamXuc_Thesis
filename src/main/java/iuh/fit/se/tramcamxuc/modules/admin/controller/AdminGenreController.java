package iuh.fit.se.tramcamxuc.modules.admin.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.genre.dto.request.GenreRequest;
import iuh.fit.se.tramcamxuc.modules.genre.dto.response.GenreResponse;
import iuh.fit.se.tramcamxuc.modules.genre.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/genres")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminGenreController {

    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(@RequestBody @Valid GenreRequest request) {
        return ResponseEntity.ok(ApiResponse.success(genreService.createGenre(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(
            @PathVariable UUID id,
            @RequestBody @Valid GenreRequest request) {
        return ResponseEntity.ok(ApiResponse.success(genreService.updateGenre(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteGenre(@PathVariable UUID id) {
        genreService.deleteGenre(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thể loại thành công"));
    }
}