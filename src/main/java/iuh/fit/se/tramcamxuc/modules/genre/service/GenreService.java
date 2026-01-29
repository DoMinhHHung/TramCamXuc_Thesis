package iuh.fit.se.tramcamxuc.modules.genre.service;

import iuh.fit.se.tramcamxuc.modules.genre.dto.response.GenreResponse;
import java.util.List;

public interface GenreService {
    List<GenreResponse> getAllGenres();
}