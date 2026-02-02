package iuh.fit.se.tramcamxuc.modules.playlist.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.service.CloudinaryService;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.AddSongRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.CreatePlaylistRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.UpdatePlaylistRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.response.PlaylistResponse;
import iuh.fit.se.tramcamxuc.modules.playlist.entity.Playlist;
import iuh.fit.se.tramcamxuc.modules.playlist.entity.PlaylistSong;
import iuh.fit.se.tramcamxuc.modules.playlist.repository.PlaylistRepository;
import iuh.fit.se.tramcamxuc.modules.playlist.repository.PlaylistSongRepository;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceImplTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistSongRepository playlistSongRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private UserService userService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private PlaylistServiceImpl playlistService;

    private User user;
    private Playlist playlist;
    private Song song;
    private PlaylistSong playlistSong;
    private UUID userId;
    private UUID playlistId;
    private UUID songId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        playlistId = UUID.randomUUID();
        songId = UUID.randomUUID();

        user = User.builder()
                .email("user@test.com")
                .fullName("Test User")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        playlist = Playlist.builder()
                .name("Test Playlist")
                .slug("test-playlist")
                .description("Test Description")
                .user(user)
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(playlist, "id", playlistId);

        song = Song.builder()
                .title("Test Song")
                .build();
        ReflectionTestUtils.setField(song, "id", songId);

        playlistSong = PlaylistSong.builder()
                .playlist(playlist)
                .song(song)
                .order(1)
                .build();
    }

    @Test
    @DisplayName("Should create playlist successfully")
    void createPlaylist_Success() {
        // Given
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("New Playlist");
        request.setDescription("Playlist Description");

        when(userService.getCurrentUser()).thenReturn(user);
        when(playlistRepository.existsBySlug(anyString())).thenReturn(false);
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        // When
        PlaylistResponse result = playlistService.createPlaylist(request, null);

        // Then
        assertNotNull(result);
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Should create playlist with thumbnail")
    void createPlaylist_WithThumbnail() {
        // Given
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("New Playlist");
        MultipartFile thumbnail = mock(MultipartFile.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(cloudinaryService.uploadImageAsync(any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture("http://thumb.url"));
        when(playlistRepository.existsBySlug(anyString())).thenReturn(false);
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        // When
        PlaylistResponse result = playlistService.createPlaylist(request, thumbnail);

        // Then
        assertNotNull(result);
        verify(cloudinaryService).uploadImageAsync(any(), anyString());
    }

    @Test
    @DisplayName("Should update playlist successfully")
    void updatePlaylist_Success() {
        // Given
        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        request.setName("Updated Name");
        request.setDescription("Updated Description");
        request.setIsPublic(false);

        when(userService.getCurrentUser()).thenReturn(user);
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        // When
        PlaylistResponse result = playlistService.updatePlaylist(playlistId, request, null);

        // Then
        assertNotNull(result);
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Should throw exception when updating playlist not owned")
    void updatePlaylist_NotOwned() {
        // Given
        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        UUID anotherUserId = UUID.randomUUID();
        User anotherUser = User.builder().email("another@test.com").build();
        ReflectionTestUtils.setField(anotherUser, "id", anotherUserId);
        playlist.setUser(anotherUser);

        when(userService.getCurrentUser()).thenReturn(user);
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

        // When & Then
        assertThrows(AppException.class, () -> 
            playlistService.updatePlaylist(playlistId, request, null)
        );
    }

    @Test
    @DisplayName("Should delete playlist successfully")
    void deletePlaylist_Success() {
        // Given
        when(userService.getCurrentUser()).thenReturn(user);
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

        // When
        playlistService.deletePlaylist(playlistId);

        // Then
        verify(playlistRepository).delete(playlist);
    }

    @Test
    @DisplayName("Should add song to playlist successfully")
    void addSongToPlaylist_Success() {
        // Given
        AddSongRequest request = new AddSongRequest();
        request.setSongId(songId);

        when(userService.getCurrentUser()).thenReturn(user);
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, songId)).thenReturn(false);
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));
        when(playlistSongRepository.findMaxOrderByPlaylistId(playlistId)).thenReturn(0);
        when(playlistSongRepository.save(any(PlaylistSong.class))).thenReturn(playlistSong);

        // When
        playlistService.addSongToPlaylist(playlistId, request);

        // Then
        verify(playlistSongRepository).save(any(PlaylistSong.class));
    }

    @Test
    @DisplayName("Should throw exception when song already in playlist")
    void addSongToPlaylist_AlreadyExists() {
        // Given
        AddSongRequest request = new AddSongRequest();
        request.setSongId(songId);

        when(userService.getCurrentUser()).thenReturn(user);
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, songId)).thenReturn(true);

        // When & Then
        assertThrows(AppException.class, () -> 
            playlistService.addSongToPlaylist(playlistId, request)
        );
    }

    @Test
    @DisplayName("Should remove song from playlist successfully")
    void removeSongFromPlaylist_Success() {
        // Given
        when(userService.getCurrentUser()).thenReturn(user);
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId))
                .thenReturn(Optional.of(playlistSong));

        // When
        playlistService.removeSongFromPlaylist(playlistId, songId);

        // Then
        verify(playlistSongRepository).delete(playlistSong);
    }

    @Test
    @DisplayName("Should get my playlists with pagination")
    void getMyPlaylists_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Playlist> playlistPage = new PageImpl<>(Collections.singletonList(playlist));

        when(userService.getCurrentUser()).thenReturn(user);
        when(playlistRepository.findByUserId(any(), any())).thenReturn(playlistPage);

        // When
        Page<PlaylistResponse> result = playlistService.getMyPlaylists(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get public playlist by id successfully")
    void getPublicPlaylist_Success() {
        // Given
        when(playlistRepository.findByIdWithSongs(playlistId)).thenReturn(Optional.of(playlist));

        // When
        PlaylistResponse result = playlistService.getPlaylistDetail(playlistId);

        // Then
        assertNotNull(result);
        assertEquals("Test Playlist", result.getName());
    }

    @Test
    @DisplayName("Should throw exception when playlist not found")
    void getPublicPlaylist_NotFound() {
        // Given
        when(playlistRepository.findByIdWithSongs(playlistId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            playlistService.getPlaylistDetail(playlistId)
        );
    }
}
