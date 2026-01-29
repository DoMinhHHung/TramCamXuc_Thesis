package iuh.fit.se.tramcamxuc.modules.genre.service;

import iuh.fit.se.tramcamxuc.modules.genre.dto.request.GenreRequest;
import iuh.fit.se.tramcamxuc.modules.genre.dto.response.GenreResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface GenreService {
    List<GenreResponse> getAllGenres();

    GenreResponse createGenre(GenreRequest request);
    GenreResponse updateGenre(UUID id, GenreRequest request);
    void deleteGenre(UUID id);
}