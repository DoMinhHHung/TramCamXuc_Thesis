package iuh.fit.se.tramcamxuc.modules.song.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.service.CloudinaryService;
import iuh.fit.se.tramcamxuc.common.service.EmailService;
import iuh.fit.se.tramcamxuc.common.service.MinioService;
import iuh.fit.se.tramcamxuc.common.utils.SlugUtils;
import iuh.fit.se.tramcamxuc.modules.artist.entity.Artist;
import iuh.fit.se.tramcamxuc.modules.artist.repository.ArtistRepository;
import iuh.fit.se.tramcamxuc.modules.genre.entity.Genre;
import iuh.fit.se.tramcamxuc.modules.genre.repository.GenreRepository;
import iuh.fit.se.tramcamxuc.modules.song.dto.request.UploadSongRequest;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import iuh.fit.se.tramcamxuc.modules.song.service.SongService;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    private final EmailService emailService;

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
        try {
            File tempFile = File.createTempFile("upload", audioFile.getOriginalFilename());
            audioFile.transferTo(tempFile);
            rawUrl = minioService.uploadMusicFileAsync(tempFile, audioFile.getContentType(), audioFile.getOriginalFilename()).join();
        } catch (Exception e) {
            throw new AppException("Error uploading music file to storage: " + e.getMessage());
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
                .status(SongStatus.DRAFT)
                .build();

        Song savedSong = songRepository.save(song);

        redisTemplate.opsForList().leftPush(TRANSCODE_QUEUE_KEY, savedSong.getId().toString());
        log.info("Pushed song {} to transcode queue", savedSong.getId());

        return SongResponse.fromEntity(savedSong);
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

        if (song.getStatus() == SongStatus.DRAFT) {
            throw new AppException("Song is still processing (transcoding) and cannot be approved.");
        }

        song.setStatus(SongStatus.PUBLIC);
        songRepository.save(song);

        emailService.sendSongStatusEmail(
                song.getArtist().getUser().getEmail(),
                song.getArtist().getArtistName(),
                song.getTitle(),
                "APPROVED",
                song.getSlug()
        );
    }

    @Override
    @Transactional
    public void rejectSong(UUID songId, String reason) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song does not exist"));

        song.setStatus(SongStatus.REJECTED);
        songRepository.save(song);

        emailService.sendSongStatusEmail(
                song.getArtist().getUser().getEmail(),
                song.getArtist().getArtistName(),
                song.getTitle(),
                "REJECTED",
                reason
        );
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
}