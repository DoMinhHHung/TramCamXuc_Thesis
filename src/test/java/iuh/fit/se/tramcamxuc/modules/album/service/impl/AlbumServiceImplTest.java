package iuh.fit.se.tramcamxuc.modules.album.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.service.CloudinaryService;
import iuh.fit.se.tramcamxuc.modules.album.dto.request.AddSongToAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.request.CreateAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.request.UpdateAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.response.AlbumResponse;
import iuh.fit.se.tramcamxuc.modules.album.entity.Album;
import iuh.fit.se.tramcamxuc.modules.album.repository.AlbumRepository;
import iuh.fit.se.tramcamxuc.modules.artist.entity.Artist;
import iuh.fit.se.tramcamxuc.modules.artist.repository.ArtistRepository;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.entity.enums.Role;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceImplTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private UserService userService;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AlbumServiceImpl albumService;

    private User user;
    private Artist artist;
    private Album album;
    private Song song;
    private UUID userId;
    private UUID artistId;
    private UUID albumId;
    private UUID songId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        artistId = UUID.randomUUID();
        albumId = UUID.randomUUID();
        songId = UUID.randomUUID();

        user = User.builder()
                .email("artist@test.com")
                .fullName("Test Artist")
                .role(Role.ARTIST)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        artist = Artist.builder()
                .user(user)
                .artistName("Test Artist")
                .bio("Test Bio")
                .build();
        ReflectionTestUtils.setField(artist, "id", artistId);

        album = Album.builder()
                .title("Test Album")
                .slug("test-album")
                .description("Test Description")
                .artist(artist)
                .songs(new ArrayList<>())
                .build();
        ReflectionTestUtils.setField(album, "id", albumId);

        song = Song.builder()
                .title("Test Song")
                .artist(artist)
                .status(SongStatus.DRAFT)
                .build();
        ReflectionTestUtils.setField(song, "id", songId);
    }

    @Test
    @DisplayName("Should create album successfully")
    void createAlbum_Success() {
        // Given
        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setTitle("New Album");
        request.setDescription("Album Description");

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(albumRepository.existsBySlug(anyString())).thenReturn(false);
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        // When
        AlbumResponse result = albumService.createAlbum(request, null);

        // Then
        assertNotNull(result);
        verify(albumRepository).save(any(Album.class));
    }

    @Test
    @DisplayName("Should create album with cover file")
    void createAlbum_WithCoverFile() {
        // Given
        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setTitle("New Album");
        MultipartFile coverFile = mock(MultipartFile.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(cloudinaryService.uploadImageAsync(any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture("http://cover.url"));
        when(albumRepository.existsBySlug(anyString())).thenReturn(false);
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        // When
        AlbumResponse result = albumService.createAlbum(request, coverFile);

        // Then
        assertNotNull(result);
        verify(cloudinaryService).uploadImageAsync(any(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when user is not artist")
    void createAlbum_UserNotArtist() {
        // Given
        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setTitle("New Album");

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AppException.class, () -> 
            albumService.createAlbum(request, null)
        );
    }

    @Test
    @DisplayName("Should update album successfully")
    void updateAlbum_Success() {
        // Given
        UpdateAlbumRequest request = new UpdateAlbumRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        // When
        AlbumResponse result = albumService.updateAlbum(albumId, request, null);

        // Then
        assertNotNull(result);
        verify(albumRepository).save(any(Album.class));
    }

    @Test
    @DisplayName("Should add songs to album successfully")
    void addSongsToAlbum_Success() {
        // Given
        AddSongToAlbumRequest request = new AddSongToAlbumRequest();
        request.setSongIds(Collections.singletonList(songId));

        song.setArtist(artist);
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(songRepository.findAllById(anyList())).thenReturn(Collections.singletonList(song));
        when(songRepository.saveAll(anyList())).thenReturn(Collections.singletonList(song));

        // When
        albumService.addSongsToAlbum(albumId, request);

        // Then
        verify(songRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when adding song from different artist")
    void addSongsToAlbum_DifferentArtist() {
        // Given
        AddSongToAlbumRequest request = new AddSongToAlbumRequest();
        request.setSongIds(Collections.singletonList(songId));

        UUID anotherArtistId = UUID.randomUUID();
        Artist anotherArtist = Artist.builder()
                .artistName("Another Artist")
                .build();
        ReflectionTestUtils.setField(anotherArtist, "id", anotherArtistId);
        song.setArtist(anotherArtist);

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(songRepository.findAllById(anyList())).thenReturn(Collections.singletonList(song));

        // When & Then
        assertThrows(AppException.class, () -> 
            albumService.addSongsToAlbum(albumId, request)
        );
    }

    @Test
    @DisplayName("Should remove song from album successfully")
    void removeSongFromAlbum_Success() {
        // Given
        song.setAlbum(album);

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));
        when(songRepository.save(any(Song.class))).thenReturn(song);

        // When
        albumService.removeSongFromAlbum(albumId, songId);

        // Then
        verify(songRepository).save(any(Song.class));
        assertNull(song.getAlbum());
    }

    @Test
    @DisplayName("Should delete album successfully")
    void deleteAlbum_Success() {
        // Given
        album.setSongs(Collections.singletonList(song));

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(songRepository.saveAll(anyList())).thenReturn(Collections.singletonList(song));

        // When
        albumService.deleteAlbum(albumId);

        // Then
        verify(albumRepository).delete(album);
    }

    @Test
    @DisplayName("Should get my albums with pagination")
    void getMyAlbums_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(Collections.singletonList(album));

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(albumRepository.findByArtistId(any(), any())).thenReturn(albumPage);

        // When
        Page<AlbumResponse> result = albumService.getMyAlbums(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should submit album for approval successfully")
    void submitAlbumForApproval_Success() {
        // Given
        song.setStatus(SongStatus.DRAFT);
        album.setSongs(Collections.singletonList(song));

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(albumRepository.findByIdWithSongs(albumId)).thenReturn(Optional.of(album));
        when(songRepository.saveAll(anyList())).thenReturn(Collections.singletonList(song));

        // When
        albumService.submitAlbumForApproval(albumId);

        // Then
        verify(songRepository).saveAll(anyList());
        assertEquals(SongStatus.PENDING_APPROVAL, song.getStatus());
    }

    @Test
    @DisplayName("Should get album detail successfully")
    void getAlbumDetail_Success() {
        // Given
        when(albumRepository.findByIdWithSongs(albumId)).thenReturn(Optional.of(album));

        // When
        AlbumResponse result = albumService.getAlbumDetail(albumId);

        // Then
        assertNotNull(result);
        assertEquals("Test Album", result.getTitle());
    }

    @Test
    @DisplayName("Should get albums by artist")
    void getAlbumsByArtist_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(Collections.singletonList(album));

        when(albumRepository.findByArtistIdOrderByReleaseDateDesc(artistId, pageable))
                .thenReturn(albumPage);

        // When
        Page<AlbumResponse> result = albumService.getAlbumsByArtist(artistId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get album detail by slug")
    void getAlbumDetailBySlug_Success() {
        // Given
        when(albumRepository.findBySlugWithSongs("test-album")).thenReturn(Optional.of(album));

        // When
        AlbumResponse result = albumService.getAlbumDetailBySlug("test-album");

        // Then
        assertNotNull(result);
        assertEquals("Test Album", result.getTitle());
    }

    @Test
    @DisplayName("Should approve album successfully")
    void approveAlbum_Success() {
        // Given
        song.setStatus(SongStatus.PENDING_APPROVAL);
        album.setSongs(Collections.singletonList(song));

        when(albumRepository.findByIdWithSongs(albumId)).thenReturn(Optional.of(album));

        // When
        albumService.approveAlbum(albumId);

        // Then
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Should reject album successfully")
    void rejectAlbum_Success() {
        // Given
        song.setStatus(SongStatus.PENDING_APPROVAL);
        album.setSongs(Collections.singletonList(song));

        when(albumRepository.findByIdWithSongs(albumId)).thenReturn(Optional.of(album));

        // When
        albumService.rejectAlbum(albumId, "Quality issues");

        // Then
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }
}
