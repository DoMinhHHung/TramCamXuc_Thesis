package iuh.fit.se.tramcamxuc.modules.song.service.impl;

import iuh.fit.se.tramcamxuc.common.event.SongApprovedEvent;
import iuh.fit.se.tramcamxuc.common.event.SongRejectedEvent;
import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.service.CloudinaryService;
import iuh.fit.se.tramcamxuc.common.service.MinioService;
import iuh.fit.se.tramcamxuc.modules.artist.entity.Artist;
import iuh.fit.se.tramcamxuc.modules.artist.repository.ArtistRepository;
import iuh.fit.se.tramcamxuc.modules.genre.entity.Genre;
import iuh.fit.se.tramcamxuc.modules.genre.repository.GenreRepository;
import iuh.fit.se.tramcamxuc.modules.song.dto.request.UploadSongRequest;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongUploadAndApprovalTest {

    @Mock
    private SongRepository songRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private UserService userService;

    @Mock
    private MinioService minioService;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SongServiceImpl songService;

    private UUID userId;
    private UUID artistId;
    private UUID genreId;
    private UUID songId;
    private User user;
    private Artist artist;
    private Genre genre;
    private Song song;
    private UploadSongRequest uploadRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        artistId = UUID.randomUUID();
        genreId = UUID.randomUUID();
        songId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        artist = new Artist();
        artist.setId(artistId);
        artist.setUser(user);
        artist.setArtistName("Test Artist");

        genre = new Genre();
        genre.setId(genreId);
        genre.setName("Pop");

        song = new Song();
        song.setId(songId);
        song.setTitle("Test Song");
        song.setArtist(artist);
        song.setGenre(genre);
        song.setStatus(SongStatus.DRAFT);
        song.setHasBeenApproved(false);

        uploadRequest = new UploadSongRequest();
        uploadRequest.setTitle("New Song");
        uploadRequest.setBio("Song description");
        uploadRequest.setGenreId(genreId);

        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    // ========== UPLOAD NHẠC TESTS ==========

    @Test
    @DisplayName("Should upload song successfully")
    void uploadSong_Success() throws Exception {
        // Given
        MultipartFile audioFile = mock(MultipartFile.class);
        MultipartFile coverFile = mock(MultipartFile.class);
        
        when(audioFile.getOriginalFilename()).thenReturn("song.mp3");
        when(audioFile.getSize()).thenReturn(5000000L);
        when(audioFile.getContentType()).thenReturn("audio/mpeg");
        when(audioFile.getInputStream()).thenReturn(new FileInputStream(File.createTempFile("test", ".mp3")));
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(minioService.uploadMusicFileAsync(any(File.class), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture("raw-url"));
        when(cloudinaryService.uploadImageAsync(any(MultipartFile.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture("cover-url"));
        
        Song savedSong = new Song();
        savedSong.setId(songId);
        savedSong.setTitle(uploadRequest.getTitle());
        savedSong.setStatus(SongStatus.PROCESSING);
        when(songRepository.save(any(Song.class))).thenReturn(savedSong);

        // When
        SongResponse result = songService.uploadSong(uploadRequest, audioFile, coverFile);

        // Then
        assertNotNull(result);
        verify(songRepository).save(any(Song.class));
        verify(listOperations).leftPush(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when user is not an artist")
    void uploadSong_UserNotArtist_ThrowsException() {
        // Given
        MultipartFile audioFile = mock(MultipartFile.class);
        when(audioFile.getOriginalFilename()).thenReturn("song.mp3");
        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AppException.class,
                () -> songService.uploadSong(uploadRequest, audioFile, null));
        verify(songRepository, never()).save(any(Song.class));
    }

    @Test
    @DisplayName("Should throw exception when genre not found")
    void uploadSong_GenreNotFound_ThrowsException() {
        // Given
        MultipartFile audioFile = mock(MultipartFile.class);
        when(audioFile.getOriginalFilename()).thenReturn("song.mp3");
        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> songService.uploadSong(uploadRequest, audioFile, null));
        verify(songRepository, never()).save(any(Song.class));
    }

    // ========== REQUEST APPROVAL TESTS ==========

    @Test
    @DisplayName("Should request approval successfully")
    void requestApproval_Success() {
        // Given
        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));

        // When
        songService.requestApproval(songId);

        // Then
        verify(songRepository).save(argThat(s ->
                s.getStatus() == SongStatus.PENDING_APPROVAL
        ));
    }

    @Test
    @DisplayName("Should throw exception when song is not in DRAFT status")
    void requestApproval_NotDraftStatus_ThrowsException() {
        // Given
        song.setStatus(SongStatus.PUBLIC);
        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));

        // When & Then
        assertThrows(AppException.class,
                () -> songService.requestApproval(songId));
    }

    @Test
    @DisplayName("Should throw exception when user is not the owner")
    void requestApproval_NotAuthorized_ThrowsException() {
        // Given
        Artist otherArtist = new Artist();
        otherArtist.setId(UUID.randomUUID());
        song.setArtist(otherArtist);

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));

        // When & Then
        assertThrows(AppException.class,
                () -> songService.requestApproval(songId));
    }

    // ========== ADMIN APPROVE TESTS ==========

    @Test
    @DisplayName("Should approve song successfully")
    void approveSong_Success() {
        // Given
        song.setStatus(SongStatus.PENDING_APPROVAL);
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));

        // When
        songService.approveSong(songId);

        // Then
        verify(songRepository).save(argThat(s ->
                s.getStatus() == SongStatus.PUBLIC && s.isHasBeenApproved()
        ));
        verify(eventPublisher).publishEvent(any(SongApprovedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when song is not PENDING_APPROVAL")
    void approveSong_NotPendingStatus_ThrowsException() {
        // Given
        song.setStatus(SongStatus.DRAFT);
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));

        // When & Then
        assertThrows(AppException.class,
                () -> songService.approveSong(songId));
        verify(songRepository, never()).save(any(Song.class));
    }

    @Test
    @DisplayName("Should throw exception when song not found for approval")
    void approveSong_SongNotFound_ThrowsException() {
        // Given
        when(songRepository.findById(songId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> songService.approveSong(songId));
    }

    // ========== ADMIN REJECT TESTS ==========

    @Test
    @DisplayName("Should reject song successfully")
    void rejectSong_Success() {
        // Given
        song.setStatus(SongStatus.PENDING_APPROVAL);
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));
        String reason = "Quality issues";

        // When
        songService.rejectSong(songId, reason);

        // Then
        verify(songRepository).save(argThat(s ->
                s.getStatus() == SongStatus.REJECTED
        ));
        verify(eventPublisher).publishEvent(any(SongRejectedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when rejecting non-pending song")
    void rejectSong_NotPendingStatus_ThrowsException() {
        // Given
        song.setStatus(SongStatus.PUBLIC);
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));

        // When & Then
        assertThrows(AppException.class,
                () -> songService.rejectSong(songId, "reason"));
        verify(songRepository, never()).save(any(Song.class));
    }

    @Test
    @DisplayName("Should throw exception when song not found for rejection")
    void rejectSong_SongNotFound_ThrowsException() {
        // Given
        when(songRepository.findById(songId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> songService.rejectSong(songId, "reason"));
    }

    // ========== TOGGLE PUBLIC/PRIVATE TESTS ==========

    @Test
    @DisplayName("Should toggle from PUBLIC to PRIVATE")
    void togglePublicPrivate_PublicToPrivate_Success() {
        // Given
        song.setStatus(SongStatus.PUBLIC);
        song.setHasBeenApproved(true);
        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));

        // When
        songService.togglePublicPrivate(songId);

        // Then
        verify(songRepository).save(argThat(s ->
                s.getStatus() == SongStatus.PRIVATE
        ));
    }

    @Test
    @DisplayName("Should toggle from PRIVATE to PUBLIC")
    void togglePublicPrivate_PrivateToPublic_Success() {
        // Given
        song.setStatus(SongStatus.PRIVATE);
        song.setHasBeenApproved(true);
        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));

        // When
        songService.togglePublicPrivate(songId);

        // Then
        verify(songRepository).save(argThat(s ->
                s.getStatus() == SongStatus.PUBLIC
        ));
    }

    @Test
    @DisplayName("Should throw exception when song has not been approved")
    void togglePublicPrivate_NotApproved_ThrowsException() {
        // Given
        song.setStatus(SongStatus.DRAFT);
        song.setHasBeenApproved(false);
        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));

        // When & Then
        assertThrows(AppException.class,
                () -> songService.togglePublicPrivate(songId));
    }
}
