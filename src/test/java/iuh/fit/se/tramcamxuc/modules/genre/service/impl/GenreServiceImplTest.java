package iuh.fit.se.tramcamxuc.modules.genre.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.modules.genre.dto.request.GenreRequest;
import iuh.fit.se.tramcamxuc.modules.genre.dto.response.GenreResponse;
import iuh.fit.se.tramcamxuc.modules.genre.entity.Genre;
import iuh.fit.se.tramcamxuc.modules.genre.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreServiceImpl genreService;

    private Genre testGenre;
    private GenreRequest genreRequest;

    @BeforeEach
    void setUp() {
        testGenre = new Genre();
        testGenre.setId(UUID.randomUUID());
        testGenre.setName("Pop");
        testGenre.setSlug("pop");
        testGenre.setDescription("Pop music genre");

        genreRequest = new GenreRequest();
        genreRequest.setName("Rock");
        genreRequest.setDescription("Rock music genre");
    }

    @Test
    @DisplayName("Should get all genres successfully")
    void getAllGenres_Success() {
        // Given
        Genre genre1 = new Genre();
        genre1.setId(UUID.randomUUID());
        genre1.setName("Pop");
        genre1.setSlug("pop");

        Genre genre2 = new Genre();
        genre2.setId(UUID.randomUUID());
        genre2.setName("Rock");
        genre2.setSlug("rock");

        when(genreRepository.findAll()).thenReturn(Arrays.asList(genre1, genre2));

        // When
        List<GenreResponse> result = genreService.getAllGenres();

        // Then
        assertEquals(2, result.size());
        verify(genreRepository).findAll();
    }

    @Test
    @DisplayName("Should create genre successfully")
    void createGenre_Success() {
        // Given
        Genre savedGenre = new Genre();
        savedGenre.setId(UUID.randomUUID());
        savedGenre.setName(genreRequest.getName());
        savedGenre.setSlug("rock");

        when(genreRepository.existsByName(anyString())).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenReturn(savedGenre);

        // When
        GenreResponse result = genreService.createGenre(genreRequest);

        // Then
        assertNotNull(result);
        verify(genreRepository).existsByName(genreRequest.getName());
        verify(genreRepository).save(any(Genre.class));
    }

    @Test
    @DisplayName("Should throw exception when creating genre with duplicate name")
    void createGenre_DuplicateName_ThrowsException() {
        // Given
        when(genreRepository.existsByName(anyString())).thenReturn(true);

        // When & Then
        assertThrows(AppException.class, () -> genreService.createGenre(genreRequest));
        verify(genreRepository, never()).save(any(Genre.class));
    }

    @Test
    @DisplayName("Should update genre successfully")
    void updateGenre_Success() {
        // Given
        UUID genreId = UUID.randomUUID();
        genreRequest.setName("Updated Pop");

        Genre updatedGenre = new Genre();
        updatedGenre.setId(genreId);
        updatedGenre.setName(genreRequest.getName());
        updatedGenre.setSlug("updated-pop");

        when(genreRepository.findById(genreId)).thenReturn(Optional.of(testGenre));
        when(genreRepository.existsByNameAndIdNot(anyString(), any(UUID.class))).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenReturn(updatedGenre);

        // When
        GenreResponse result = genreService.updateGenre(genreId, genreRequest);

        // Then
        assertNotNull(result);
        verify(genreRepository).save(any(Genre.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent genre")
    void updateGenre_NotFound_ThrowsException() {
        // Given
        UUID genreId = UUID.randomUUID();
        when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                () -> genreService.updateGenre(genreId, genreRequest));
    }

    @Test
    @DisplayName("Should delete genre successfully")
    void deleteGenre_Success() {
        // Given
        UUID genreId = UUID.randomUUID();
        when(genreRepository.existsById(genreId)).thenReturn(true);

        // When
        genreService.deleteGenre(genreId);

        // Then
        verify(genreRepository).deleteById(genreId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent genre")
    void deleteGenre_NotFound_ThrowsException() {
        // Given
        UUID genreId = UUID.randomUUID();
        when(genreRepository.existsById(genreId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                () -> genreService.deleteGenre(genreId));
    }
}
