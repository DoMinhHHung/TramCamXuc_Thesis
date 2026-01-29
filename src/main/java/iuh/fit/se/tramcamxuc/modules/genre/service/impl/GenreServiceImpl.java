package iuh.fit.se.tramcamxuc.modules.genre.service.impl;

import iuh.fit.se.tramcamxuc.modules.genre.dto.response.GenreResponse;
import iuh.fit.se.tramcamxuc.modules.genre.repository.GenreRepository;
import iuh.fit.se.tramcamxuc.modules.genre.service.GenreService;
import jakarta.persistence.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    @Override
    @Cacheable("all_genres")
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(GenreResponse::fromEntity)
                .collect(Collectors.toList());
    }
}