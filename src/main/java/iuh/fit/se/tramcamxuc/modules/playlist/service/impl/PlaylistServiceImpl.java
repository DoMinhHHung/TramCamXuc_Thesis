package iuh.fit.se.tramcamxuc.modules.playlist.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.service.CloudinaryService;
import iuh.fit.se.tramcamxuc.common.utils.SlugUtils;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.AddSongRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.CreatePlaylistRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.UpdatePlaylistRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.response.PlaylistResponse;
import iuh.fit.se.tramcamxuc.modules.playlist.entity.Playlist;
import iuh.fit.se.tramcamxuc.modules.playlist.entity.PlaylistSong;
import iuh.fit.se.tramcamxuc.modules.playlist.repository.PlaylistRepository;
import iuh.fit.se.tramcamxuc.modules.playlist.repository.PlaylistSongRepository;
import iuh.fit.se.tramcamxuc.modules.playlist.service.PlaylistService;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    // --- HELPER: Generate Slug ---
    private String generateUniqueSlug(String name) {
        String baseSlug = SlugUtils.toSlug(name);
        String slug = baseSlug;
        int count = 1;
        while (playlistRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count;
            count++;
        }
        return slug;
    }

    private Playlist getOwnedPlaylist(UUID playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        User currentUser = userService.getCurrentUser();
        if (!playlist.getUser().getId().equals(currentUser.getId())) {
            throw new AppException("You are not the owner of this playlist");
        }
        return playlist;
    }

    @Override
    @Transactional
    public PlaylistResponse createPlaylist(CreatePlaylistRequest request, MultipartFile thumbnail) {
        User currentUser = userService.getCurrentUser();

        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbnailUrl = cloudinaryService.uploadImageAsync(thumbnail, "tramcamxuc/playlists").join();
        }

        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(generateUniqueSlug(request.getName()))
                .isPublic(request.isPublic())
                .thumbnailUrl(thumbnailUrl)
                .user(currentUser)
                .build();

        return PlaylistResponse.fromEntity(playlistRepository.save(playlist));
    }

    @Override
    @Transactional
    public PlaylistResponse updatePlaylist(UUID playlistId, UpdatePlaylistRequest request, MultipartFile thumbnail) {
        Playlist playlist = getOwnedPlaylist(playlistId);

        if (request.getName() != null && !request.getName().equals(playlist.getName())) {
            playlist.setName(request.getName());
            playlist.setSlug(generateUniqueSlug(request.getName()));
        }
        if (request.getDescription() != null) playlist.setDescription(request.getDescription());
        if (request.getIsPublic() != null) playlist.setPublic(request.getIsPublic());

        if (thumbnail != null && !thumbnail.isEmpty()) {
            String newThumb = cloudinaryService.uploadImageAsync(thumbnail, "tramcamxuc/playlists").join();
            playlist.setThumbnailUrl(newThumb);
        }

        return PlaylistResponse.fromEntity(playlistRepository.save(playlist));
    }

    @Override
    @Transactional
    public void deletePlaylist(UUID playlistId) {
        Playlist playlist = getOwnedPlaylist(playlistId);
        playlistRepository.delete(playlist);
    }

    @Override
    @Transactional
    public void addSongToPlaylist(UUID playlistId, AddSongRequest request) {
        Playlist playlist = getOwnedPlaylist(playlistId);

        if (playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, request.getSongId())) {
            throw new AppException("This song is already in the playlist");
        }

        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        int maxOrder = playlistSongRepository.findMaxOrderByPlaylistId(playlistId);

        PlaylistSong playlistSong = PlaylistSong.builder()
                .playlist(playlist)
                .song(song)
                .order(maxOrder + 1)
                .addedAt(LocalDateTime.now())
                .build();

        playlistSongRepository.save(playlistSong);

        if (playlist.getThumbnailUrl() == null && song.getCoverUrl() != null) {
            playlist.setThumbnailUrl(song.getCoverUrl());
            playlistRepository.save(playlist);
        }
    }

    @Override
    @Transactional
    public void removeSongFromPlaylist(UUID playlistId, UUID songId) {
        Playlist playlist = getOwnedPlaylist(playlistId);

        PlaylistSong playlistSong = playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                .orElseThrow(() -> new ResourceNotFoundException("This song is not in the playlist"));

        playlistSongRepository.delete(playlistSong);
    }

    @Override
    public Page<PlaylistResponse> getMyPlaylists(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return playlistRepository.findByUserId(currentUser.getId(), pageable)
                .map(PlaylistResponse::fromEntity);
    }

    @Override
    public PlaylistResponse getPlaylistDetail(UUID id) {
        Playlist playlist = playlistRepository.findByIdWithSongs(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        // Logic phụ: Nếu là Private playlist và người xem không phải chủ -> Chặn
        // User currentUser = userUtils.getCurrentUserOrNull(); // Cần handle trường hợp guest
        // if (!playlist.isPublic() && (currentUser == null || !playlist.getUser().getId().equals(currentUser.getId()))) {
        //    throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, "Playlist này là riêng tư");
        // }

        return PlaylistResponse.fromEntity(playlist);
    }

    @Override
    public PlaylistResponse getPlaylistBySlug(String slug) {
        Playlist playlist = playlistRepository.findBySlugWithSongs(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        if (!playlist.isPublic()) {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null || !playlist.getUser().getId().equals(currentUser.getId())) {
                throw new AppException("The playlist is private");
            }
        }

        return PlaylistResponse.fromEntity(playlist);
    }

    @Override
    public Page<PlaylistResponse> getPublicPlaylists(Pageable pageable) {
        return playlistRepository.findByIsPublicTrue(pageable)
                .map(PlaylistResponse::fromEntity);
    }
}