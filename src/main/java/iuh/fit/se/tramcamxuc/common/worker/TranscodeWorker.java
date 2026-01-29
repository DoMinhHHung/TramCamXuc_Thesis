package iuh.fit.se.tramcamxuc.common.worker;

import iuh.fit.se.tramcamxuc.common.service.MinioService;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class TranscodeWorker {

    private final StringRedisTemplate redisTemplate;
    private final SongRepository songRepository;
    private final MinioService minioService;

    private static final String QUEUE_KEY = "music:transcode:queue";
    private static final String DLQ_KEY = "music:transcode:dead";
    private static final String RETRY_KEY_PREFIX = "music:transcode:retry:";
    private static final int MAX_RETRY = 3;

    @Scheduled(fixedDelay = 5000)
    public void processTranscode() {
        String songIdStr = redisTemplate.opsForList().rightPop(QUEUE_KEY);
        if (songIdStr == null) return;

        String lockKey = "LOCK:TRANSCODE_WORKER:" + songIdStr;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofMinutes(10));
        if (Boolean.FALSE.equals(acquired)) return;

        log.info(">>> [WORKER] Bắt đầu Transcode bài hát ID: {}", songIdStr);

        File tempInDir = null;
        File tempOutDir = null;
        UUID songId = UUID.fromString(songIdStr);

        try {
            Song song = songRepository.findById(songId).orElse(null);
            if (song == null) {
                log.warn("Bài hát không tồn tại trong DB, bỏ qua: {}", songId);
                return;
            }

            tempInDir = Files.createTempDirectory("transcode_in_" + songId).toFile();
            tempOutDir = Files.createTempDirectory("transcode_out_" + songId).toFile();

            log.info("Đang tải file gốc: {}", song.getRawUrl());
            File inputFile = minioService.downloadFile(song.getRawUrl(), tempInDir);

            log.info("Đang chạy FFmpeg để tạo HLS...");
            String outputFileName = "playlist.m3u8";
            File outputFile = new File(tempOutDir, outputFileName);

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", inputFile.getAbsolutePath(),
                    "-c:a", "aac", "-b:a", "128k",
                    "-vn",
                    "-hls_time", "10",
                    "-hls_list_size", "0",
                    "-f", "hls",
                    outputFile.getAbsolutePath()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);

            if (!finished || process.exitValue() != 0) {
                throw new RuntimeException("FFmpeg lỗi hoặc timeout (Exit code: " + process.exitValue() + ")");
            }

            log.info("Transcode xong. Đang upload HLS lên MinIO...");
            String remotePrefix = "hls/" + songId;
            String hlsUrl = minioService.uploadFolder(tempOutDir, remotePrefix);

            if (hlsUrl == null) throw new RuntimeException("Lỗi không lấy được URL playlist");

            updateSongStatus(songId, hlsUrl, SongStatus.PENDING_APPROVAL);

            redisTemplate.delete(RETRY_KEY_PREFIX + songIdStr);
            log.info(">>> [SUCCESS] Transcode xong bài: {}", songId);

        } catch (Exception e) {
            log.error(">>> [ERROR] Lỗi Transcode bài {}: {}", songIdStr, e.getMessage());
            handleFailure(songIdStr, e.getMessage());
        } finally {
            redisTemplate.delete(lockKey);
            if (tempInDir != null) FileSystemUtils.deleteRecursively(tempInDir);
            if (tempOutDir != null) FileSystemUtils.deleteRecursively(tempOutDir);
        }
    }

    private void handleFailure(String songIdStr, String errorMessage) {
        String retryKey = RETRY_KEY_PREFIX + songIdStr;
        Long currentRetry = redisTemplate.opsForValue().increment(retryKey);

        if (currentRetry != null && currentRetry <= MAX_RETRY) {
            log.warn("Retry lần {}/{} cho bài {}", currentRetry, MAX_RETRY, songIdStr);
            redisTemplate.opsForList().leftPush(QUEUE_KEY, songIdStr);
        } else {
            log.error("Đã hết lượt Retry. Đẩy bài {} vào Dead Letter Queue.", songIdStr);
            redisTemplate.opsForList().leftPush(DLQ_KEY, songIdStr);
            redisTemplate.delete(retryKey);
            try {
                updateSongStatus(UUID.fromString(songIdStr), null, SongStatus.TRANSCODE_FAILED);
            } catch (Exception ex) {
                log.error("Lỗi update status failed", ex);
            }
        }
    }

    @Transactional
    public void updateSongStatus(UUID songId, String hlsUrl, SongStatus status) {
        Song song = songRepository.findById(songId).orElseThrow();
        if (hlsUrl != null) song.setAudioUrl(hlsUrl);
        song.setStatus(status);
        songRepository.save(song);
    }
}