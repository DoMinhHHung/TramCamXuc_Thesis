package iuh.fit.se.tramcamxuc.modules.song.service.impl;

import iuh.fit.se.tramcamxuc.common.event.SongApprovedEvent;
import iuh.fit.se.tramcamxuc.common.event.SongListenedEvent;
import iuh.fit.se.tramcamxuc.common.event.SongRejectedEvent;
import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.service.CloudinaryService;
import iuh.fit.se.tramcamxuc.common.service.MinioService;
import iuh.fit.se.tramcamxuc.common.utils.SlugUtils;
import iuh.fit.se.tramcamxuc.modules.artist.entity.Artist;
import iuh.fit.se.tramcamxuc.modules.artist.repository.ArtistRepository;
import iuh.fit.se.tramcamxuc.modules.genre.entity.Genre;
import iuh.fit.se.tramcamxuc.modules.genre.repository.GenreRepository;
import iuh.fit.se.tramcamxuc.modules.song.dto.request.UpdateSongMetadataRequest;
import iuh.fit.se.tramcamxuc.modules.song.dto.request.UploadSongRequest;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongWithAdResponse;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import iuh.fit.se.tramcamxuc.modules.song.service.SongService;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import iuh.fit.se.tramcamxuc.modules.advertisement.service.AdvertisementService;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.response.AdResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final GenreRepository genreRepository;
    private final UserService userService;
    private final MinioService minioService;
    private final CloudinaryService cloudinaryService;
    private final StringRedisTemplate redisTemplate;
    private final AdvertisementService advertisementService;
    private final ApplicationEventPublisher eventPublisher;

    private static final String TRANSCODE_QUEUE_KEY = "music:transcode:queue";

    @Override
    @Transactional
    public SongResponse uploadSong(UploadSongRequest request, MultipartFile audioFile, MultipartFile coverFile) {
        var currentUser = userService.getCurrentUser();
        Artist artist = artistRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException("You must register as an Artist before uploading music!"));

        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new ResourceNotFoundException("Genre does not exist"));

        int duration = getDurationFromMultipartFile(audioFile);

        String rawUrl;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload", audioFile.getOriginalFilename());
            audioFile.transferTo(tempFile);
            rawUrl = minioService.uploadMusicFileAsync(tempFile, audioFile.getContentType(), audioFile.getOriginalFilename()).join();
        } catch (Exception e) {
            throw new AppException("Error uploading music file to storage: " + e.getMessage());
        } finally {
            // Cleanup temp file ngay sau khi upload
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Failed to delete upload temp file: {}", tempFile.getAbsolutePath());
                }
            }
        }

        String coverUrl = null;
        if (coverFile != null && !coverFile.isEmpty()) {
            coverUrl = cloudinaryService.uploadImageAsync(coverFile, "tramcamxuc/songs/covers").join();
        }

        Song song = Song.builder()
                .title(request.getTitle())
                .slug(SlugUtils.generateSlug(request.getTitle()))
                .bio(request.getBio())
                .artist(artist)
                .genre(genre)
                .duration(duration)
                .rawUrl(rawUrl)
                .coverUrl(coverUrl)
                .status(SongStatus.PROCESSING)
                .hasBeenApproved(false)
                .build();

        Song savedSong = songRepository.save(song);

        redisTemplate.opsForList().leftPush(TRANSCODE_QUEUE_KEY, savedSong.getId().toString());
        log.info("Pushed song {} to transcode queue", savedSong.getId());

        return SongResponse.fromEntity(savedSong);
    }

    // --- ARTIST METHODS ---

    @Override
    @Transactional
    public SongResponse updateSongMetadata(UUID songId, UpdateSongMetadataRequest request, MultipartFile coverFile) {
        var currentUser = userService.getCurrentUser();
        Artist artist = artistRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException("You are not registered as an artist"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        // Chỉ artist sở hữu mới được sửa
        if (!song.getArtist().getId().equals(artist.getId())) {
            throw new AppException("You are not authorized to edit this song");
        }

        if (song.getStatus() != SongStatus.DRAFT) {
            throw new AppException("Only songs in DRAFT status can be edited. Current status: " + song.getStatus());
        }

        song.setTitle(request.getTitle());
        song.setSlug(SlugUtils.generateSlug(request.getTitle()));
        song.setBio(request.getBio());

        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found"));
        song.setGenre(genre);

        if (coverFile != null && !coverFile.isEmpty()) {
            String newCoverUrl = cloudinaryService.uploadImageAsync(coverFile, "tramcamxuc/songs/covers").join();
            if (song.getCoverUrl() != null) {
                cloudinaryService.deleteImage(song.getCoverUrl());
            }
            song.setCoverUrl(newCoverUrl);
        }

        Song updatedSong = songRepository.save(song);
        return SongResponse.fromEntity(updatedSong);
    }

    @Override
    @Transactional
    public void requestApproval(UUID songId) {
        var currentUser = userService.getCurrentUser();
        Artist artist = artistRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException("You are not registered as an artist"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        if (!song.getArtist().getId().equals(artist.getId())) {
            throw new AppException("You are not authorized to modify this song");
        }

        if (song.isHasBeenApproved() && song.getStatus() == SongStatus.PRIVATE) {
            song.setStatus(SongStatus.PUBLIC);
            songRepository.save(song);
            log.info("Song {} changed from PRIVATE to PUBLIC (already approved)", songId);
            return;
        }

        if (song.getStatus() != SongStatus.DRAFT) {
            throw new AppException("Only DRAFT songs can request approval. Current status: " + song.getStatus());
        }

        song.setStatus(SongStatus.PENDING_APPROVAL);
        songRepository.save(song);
        
        log.info("Song {} requested approval by artist {}", songId, artist.getArtistName());
    }

    @Override
    @Transactional
    public void togglePublicPrivate(UUID songId) {
        var currentUser = userService.getCurrentUser();
        Artist artist = artistRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException("You are not registered as an artist"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        if (!song.getArtist().getId().equals(artist.getId())) {
            throw new AppException("You are not authorized to modify this song");
        }

        if (!song.isHasBeenApproved()) {
            throw new AppException("Song has not been approved yet. Please request approval first.");
        }

        if (song.getStatus() == SongStatus.PUBLIC) {
            song.setStatus(SongStatus.PRIVATE);
            log.info("Song {} changed to PRIVATE by artist {}", songId, artist.getArtistName());
        } else if (song.getStatus() == SongStatus.PRIVATE) {
            song.setStatus(SongStatus.PUBLIC);
            log.info("Song {} changed to PUBLIC by artist {}", songId, artist.getArtistName());
        } else {
            throw new AppException("Only PUBLIC or PRIVATE songs can be toggled. Current status: " + song.getStatus());
        }

        songRepository.save(song);
    }

    @Override
    public Page<SongResponse> getMySongs(Pageable pageable) {
        var currentUser = userService.getCurrentUser();
        Artist artist = artistRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException("You are not registered as an artist"));

        return songRepository.findByArtistIdAndStatusNot(artist.getId(), SongStatus.DELETED, pageable)
                .map(SongResponse::fromEntity);
    }

    // --- ADMIN METHODS ---

    @Override
    public Page<SongResponse> getAdminSongs(String keyword, SongStatus status, Pageable pageable) {
        return songRepository.findForAdmin(keyword, status, pageable)
                .map(SongResponse::fromEntity);
    }

    @Override
    @Transactional
    public void approveSong(UUID songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song does not exist"));

        if (song.getStatus() != SongStatus.PENDING_APPROVAL) {
            throw new AppException("Only songs in PENDING_APPROVAL status can be approved. Current status: " + song.getStatus());
        }

        song.setStatus(SongStatus.PUBLIC);
        song.setHasBeenApproved(true);
        songRepository.save(song);

        // Publish event instead of direct service call
        eventPublisher.publishEvent(new SongApprovedEvent(
            song.getId(),
            song.getArtist().getUser().getEmail(),
            song.getArtist().getArtistName(),
            song.getTitle(),
            song.getSlug()
        ));
    }

    @Override
    @Transactional
    public void rejectSong(UUID songId, String reason) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song does not exist"));

        if (song.getStatus() != SongStatus.PENDING_APPROVAL) {
            throw new AppException("Only songs in PENDING_APPROVAL status can be rejected");
        }

        song.setStatus(SongStatus.REJECTED);
        songRepository.save(song);

        // Publish event instead of direct service call
        eventPublisher.publishEvent(new SongRejectedEvent(
            song.getId(),
            song.getArtist().getUser().getEmail(),
            song.getArtist().getArtistName(),
            song.getTitle(),
            reason
        ));
    }

    @Override
    public Page<SongResponse> getSongsByStatusForAdmin(SongStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());


        return songRepository.findForAdmin(null, status, pageable)
                .map(SongResponse::fromEntity);
    }

    @Override
    @Async("taskExecutor")
    public void recordListen(UUID songId) {
        String redisKey = "song_view:" + songId;
        redisTemplate.opsForValue().increment(redisKey, 1);
        redisTemplate.expire(redisKey, Duration.ofHours(1));

        // Get current user (có thể null nếu guest)
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            log.debug("Guest user listening to song: {}", songId);
        }

        // Publish event instead of direct service call
        eventPublisher.publishEvent(new SongListenedEvent(
            songId,
            currentUser != null ? currentUser.getId() : null
        ));
    }

    @Override
    @Cacheable(value = "top5Trending", unless = "#result == null || #result.isEmpty()")
    public List<SongResponse> getTop5Trending() {
        List<Song> topSongs = songRepository.findTop5ByOrderByPlayCountDesc();

        return topSongs.stream()
                .map(SongResponse::fromEntity)
                .toList();
    }

    // --- METHODs HELPER ---
    private int getDurationFromMultipartFile(MultipartFile file) {
        File tempFile = null;
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) throw new AppException("Invalid file name");

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!Set.of(".mp3", ".wav", ".flac", ".m4a", ".mp4", ".ogg").contains(extension)) {
                throw new AppException("Unsupported file format: " + extension);
            }

            tempFile = File.createTempFile("duration_check_" + UUID.randomUUID(), extension);
            try (var is = file.getInputStream()) {
                Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            if (".mp4".equals(extension) || ".m4a".equals(extension)) {
                try (IsoFile isoFile = new IsoFile(tempFile)) {
                    MovieHeaderBox mvhd = isoFile.getMovieBox().getMovieHeaderBox();
                    return (int) (mvhd.getDuration() / mvhd.getTimescale());
                }
            } else {
                AudioFile audioFile = AudioFileIO.read(tempFile);
                return audioFile.getAudioHeader().getTrackLength();
            }

        } catch (Exception e) {
            log.error("Error reading duration: {}", e.getMessage());
            throw new AppException("Unable to read audio file information. The file may be corrupted.");
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Failed to delete temp file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public SongWithAdResponse getSongWithAdInfo(UUID songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));
        
        if (song.getStatus() != SongStatus.PUBLIC) {
            throw new AppException("Song is not available");
        }
        
        boolean shouldShowAd = userService.shouldShowAds();
        
        AdResponse adResponse = null;
        if (shouldShowAd) {
            adResponse = advertisementService.getRandomAdvertisement();
            if (adResponse != null) {
                advertisementService.recordImpression(adResponse.getId());
            }
        }
        
        return SongWithAdResponse.builder()
                .song(SongResponse.fromEntity(song))
                .shouldShowAd(shouldShowAd)
                .advertisement(adResponse)
                .build();
    }
}