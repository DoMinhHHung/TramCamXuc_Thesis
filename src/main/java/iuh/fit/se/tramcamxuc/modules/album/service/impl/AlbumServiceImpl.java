package iuh.fit.se.tramcamxuc.modules.album.service.impl;

import iuh.fit.se.tramcamxuc.common.event.AlbumApprovedEvent;
import iuh.fit.se.tramcamxuc.common.event.AlbumRejectedEvent;
import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.service.CloudinaryService;
import iuh.fit.se.tramcamxuc.common.utils.SlugUtils;
import iuh.fit.se.tramcamxuc.modules.album.dto.request.AddSongToAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.request.CreateAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.request.UpdateAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.response.AlbumResponse;
import iuh.fit.se.tramcamxuc.modules.album.entity.Album;
import iuh.fit.se.tramcamxuc.modules.album.repository.AlbumRepository;
import iuh.fit.se.tramcamxuc.modules.album.service.AlbumService;
import iuh.fit.se.tramcamxuc.modules.artist.entity.Artist;
import iuh.fit.se.tramcamxuc.modules.artist.repository.ArtistRepository;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService {
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final ApplicationEventPublisher eventPublisher;

    private Artist getCurrentArtist() {
        User user = userService.getCurrentUser();
        return artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException("You are not an artist!"));
    }

    private Album getOwnedAlbum(UUID albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with id: " + albumId));
        Artist currentArtist = getCurrentArtist();

        if (!album.getArtist().getId().equals(currentArtist.getId())) {
            throw new AppException("You do not have permission to access and modify this album.");
        }
        return album;
    }

    private void addSongsInternal(Album album, List<UUID> songIds, Artist owner) {
        List<Song> songs = songRepository.findAllById(songIds);

        for (Song song : songs) {
            if (!song.getArtist().getId().equals(owner.getId())) {
                throw new AppException("The song " + song.getTitle() + " not belong to you.");
            }
            if (song.getAlbum() != null && !song.getAlbum().getId().equals(album.getId())) {
                throw new AppException("The song " + song.getTitle() + " already belongs to another album.");
            }

            song.setAlbum(album);
        }
        songRepository.saveAll(songs);
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = SlugUtils.toSlug(title);
        String slug = baseSlug;
        int count = 1;

        while (albumRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count;
            count++;
        }
        return slug;
    }

    @Override
    @Transactional
    public AlbumResponse createAlbum(CreateAlbumRequest request, MultipartFile coverFile) {
        Artist artist = getCurrentArtist();

        String coverUrl = null;
        if (coverFile != null && !coverFile.isEmpty()) {
            coverUrl = cloudinaryService.uploadImageAsync(coverFile, "tramcamxuc/albums").join();
        }

        String slug = generateUniqueSlug(request.getTitle());

        Album album = Album.builder()
                .title(request.getTitle())
                .slug(slug)
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .coverUrl(coverUrl)
                .artist(artist)
                .build();

        Album savedAlbum = albumRepository.save(album);

        if (request.getSongIds() != null && !request.getSongIds().isEmpty()) {
            addSongsInternal(savedAlbum, request.getSongIds(), artist);
        }

        return AlbumResponse.fromEntity(savedAlbum);
    }

    @Override
    @Transactional
    public AlbumResponse updateAlbum(UUID albumId, UpdateAlbumRequest request, MultipartFile coverFile) {
        Album album = getOwnedAlbum(albumId);

        if (request.getTitle() != null) album.setTitle(request.getTitle());
        if (request.getDescription() != null) album.setDescription(request.getDescription());
        if (request.getReleaseDate() != null) album.setReleaseDate(request.getReleaseDate());
        if (request.getTitle() != null && !request.getTitle().equals(album.getTitle())) {
            album.setTitle(request.getTitle());
            album.setSlug(generateUniqueSlug(request.getTitle()));
        }

        if (coverFile != null && !coverFile.isEmpty()) {
            String newCover = cloudinaryService.uploadImageAsync(coverFile, "tramcamxuc/albums").join();
            album.setCoverUrl(newCover);
        }

        return AlbumResponse.fromEntity(albumRepository.save(album));
    }

    @Override
    @Transactional
    public void addSongsToAlbum(UUID albumId, AddSongToAlbumRequest request) {
        Album album = getOwnedAlbum(albumId);
        addSongsInternal(album, request.getSongIds(), album.getArtist());
    }

    @Override
    @Transactional
    public void removeSongFromAlbum(UUID albumId, UUID songId) {
        Album album = getOwnedAlbum(albumId);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        if (song.getAlbum() == null || !song.getAlbum().getId().equals(albumId)) {
            throw new AppException("Thís song does not belong to the specified album.");
        }

        song.setAlbum(null);
        songRepository.save(song);
    }

    @Override
    @Transactional
    public void deleteAlbum(UUID albumId) {
        Album album = getOwnedAlbum(albumId);

        List<Song> songs = album.getSongs();
        if (songs != null && !songs.isEmpty()) {
            songs.forEach(s -> s.setAlbum(null));
            songRepository.saveAll(songs);
        }

        albumRepository.delete(album);
    }

    @Override
    public Page<AlbumResponse> getMyAlbums(Pageable pageable) {
        Artist artist = getCurrentArtist();
        return albumRepository.findByArtistId(artist.getId(), pageable)
                .map(AlbumResponse::fromEntity);
    }

    // --- (Pre-Approval & Scheduled) ---

    @Override
    @Transactional
    public void submitAlbumForApproval(UUID albumId) {
        Album album = getOwnedAlbum(albumId);

        Album fullAlbum = albumRepository.findByIdWithSongs(albumId).orElseThrow();

        if (fullAlbum.getSongs().isEmpty()) {
            throw new AppException("Album has no songs to submit for approval. Please add songs first.");
        }

        List<Song> draftSongs = fullAlbum.getSongs().stream()
                .filter(s -> s.getStatus() == SongStatus.DRAFT)
                .toList();

        if (draftSongs.isEmpty()) {
            throw new AppException("Don't have any DRAFT songs in the album to submit for approval.");
        }

        for (Song s : draftSongs) {
            if (s.getCoverUrl() == null || s.getCoverUrl().isEmpty()) {
                s.setCoverUrl(album.getCoverUrl());
            }

            if (s.getTitle() == null) {
                throw new AppException("The song " + s.getId() + " need name.");
            }

            s.setStatus(SongStatus.PENDING_APPROVAL);

            if (album.getReleaseDate() != null) {
                s.setReleaseDate(album.getReleaseDate());
            }
        }

        songRepository.saveAll(draftSongs);
        log.info("Artist {} submitted album {} with {} songs", album.getArtist().getArtistName(), albumId, draftSongs.size());
    }

    // --- PUBLIC API ---
    @Override
    public AlbumResponse getAlbumDetail(UUID albumId) {
        Album album = albumRepository.findByIdWithSongs(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));

        return AlbumResponse.fromEntity(album);
    }

    @Override
    public Page<AlbumResponse> getAlbumsByArtist(UUID artistId, Pageable pageable) {
        return albumRepository.findByArtistIdOrderByReleaseDateDesc(artistId, pageable)
                .map(AlbumResponse::fromEntity);
    }

    @Override
    public AlbumResponse getAlbumDetailBySlug(String slug) {
        Album album = albumRepository.findBySlugWithSongs(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));
        return AlbumResponse.fromEntity(album);
    }

    @Override
    @Transactional
    public void approveAlbum(UUID albumId) {
        Album album = albumRepository.findByIdWithSongs(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));

        List<Song> pendingSongs = album.getSongs().stream()
                .filter(s -> s.getStatus() == SongStatus.PENDING_APPROVAL)
                .toList();

        if (pendingSongs.isEmpty()) {
            throw new AppException("This album has no songs to approve.");
        }
        LocalDateTime now = LocalDateTime.now();
        SongStatus targetStatus = SongStatus.PUBLIC;

        if (album.getReleaseDate() != null && album.getReleaseDate().isAfter(now)) {
            targetStatus = SongStatus.SCHEDULED;
        }

        for (Song song : pendingSongs) {
            song.setStatus(targetStatus);
            song.setHasBeenApproved(true);

            if (targetStatus == SongStatus.PUBLIC) {
                song.setReleaseDate(now);
            } else {
                song.setReleaseDate(album.getReleaseDate());
            }
        }

        songRepository.saveAll(pendingSongs);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String releaseDate = album.getReleaseDate() != null 
            ? album.getReleaseDate().format(formatter) 
            : "Ngay bây giờ";
        
        // Publish event instead of direct service call
        eventPublisher.publishEvent(new AlbumApprovedEvent(
            album.getId(),
            album.getArtist().getUser().getEmail(),
            album.getArtist().getArtistName(),
            album.getTitle(),
            album.getCoverUrl(),
            pendingSongs.size(),
            releaseDate,
            targetStatus.name(),
            album.getSlug()
        ));

        log.info("Admin approved album {} ({} songs changed to {})", album.getTitle(), pendingSongs.size(), targetStatus);
    }

    @Override
    @Transactional
    public void rejectAlbum(UUID albumId, String reason) {
        Album album = albumRepository.findByIdWithSongs(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));

        List<Song> pendingSongs = album.getSongs().stream()
                .filter(s -> s.getStatus() == SongStatus.PENDING_APPROVAL)
                .toList();

        if (pendingSongs.isEmpty()) {
            throw new AppException("This album has no songs to reject.");
        }

        for (Song song : pendingSongs) {
            song.setStatus(SongStatus.REJECTED);
        }

        songRepository.saveAll(pendingSongs);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String submittedDate = album.getCreatedAt() != null 
            ? album.getCreatedAt().format(formatter) 
            : LocalDateTime.now().format(formatter);
        
        // Publish event instead of direct service call
        eventPublisher.publishEvent(new AlbumRejectedEvent(
            album.getId(),
            album.getArtist().getUser().getEmail(),
            album.getArtist().getArtistName(),
            album.getTitle(),
            album.getCoverUrl(),
            pendingSongs.size(),
            submittedDate,
            reason,
            album.getId().toString()
        ));

        log.info("Admin rejected album {}. Reason: {}", album.getTitle(), reason);
    }
}
