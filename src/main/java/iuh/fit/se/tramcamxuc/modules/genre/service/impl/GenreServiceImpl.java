package iuh.fit.se.tramcamxuc.modules.genre.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.utils.SlugUtils;
import iuh.fit.se.tramcamxuc.modules.genre.dto.request.GenreRequest;
import iuh.fit.se.tramcamxuc.modules.genre.dto.response.GenreResponse;
import iuh.fit.se.tramcamxuc.modules.genre.entity.Genre;
import iuh.fit.se.tramcamxuc.modules.genre.repository.GenreRepository;
import iuh.fit.se.tramcamxuc.modules.genre.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
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

    @Override
    @Transactional
    @CacheEvict(value = "all_genres", allEntries = true)
    public GenreResponse createGenre(GenreRequest request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new AppException("Genre name '" + request.getName() + "' is already!");
        }
        String slug = SlugUtils.toSlug(request.getName());
        Genre genre = Genre.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .build();

        return GenreResponse.fromEntity(genreRepository.save(genre));
    }

    @Override
    @Transactional
    @CacheEvict(value = "all_genres", allEntries = true)
    public GenreResponse updateGenre(UUID id, GenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with name: " + request.getName()));

        if (genreRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException("Genre name '" + request.getName() + "' is already!");
        }

        genre.setName(request.getName());
        genre.setSlug(SlugUtils.toSlug(request.getName()));
        genre.setDescription(request.getDescription());
        return GenreResponse.fromEntity(genreRepository.save(genre));
    }

    @Override
    @Transactional
    @CacheEvict(value = "all_genres", allEntries = true)
    public void deleteGenre(UUID id) {
        if (!genreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy thể loại để xóa");
        }
        genreRepository.deleteById(id);
    }
}