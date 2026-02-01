package iuh.fit.se.tramcamxuc.common.worker;

import iuh.fit.se.tramcamxuc.common.constants.RedisKeys;
import iuh.fit.se.tramcamxuc.common.service.MinioService;
import iuh.fit.se.tramcamxuc.common.service.WorkerMetricsService;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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
    private final WorkerMetricsService workerMetricsService;

    private static final int MAX_RETRY = 3;
    private static final String LOCK_VALUE = UUID.randomUUID().toString(); // Unique lock value per worker instance

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(name = "transcodeWorker", lockAtMostFor = "10m", lockAtLeastFor = "30s")
    public void processTranscode() {
        String songIdStr = null;
        String lockKey = null;
        
        try {
            songIdStr = redisTemplate.opsForList().rightPop(RedisKeys.TRANSCODE_QUEUE, 3, TimeUnit.SECONDS);
            if (songIdStr == null) return;

            lockKey = RedisKeys.TRANSCODE_LOCK_PREFIX + songIdStr;
            String lockIdentifier = LOCK_VALUE + ":" + Thread.currentThread().getId();
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockIdentifier, Duration.ofMinutes(10));
            if (Boolean.FALSE.equals(acquired)) {
                log.warn("Bài {} đang được xử lý bởi worker khác", songIdStr);
                return;
            }

            log.info(">>> [WORKER] Bắt đầu Transcode bài hát ID: {} với lock: {}", songIdStr, lockIdentifier);
            
        } catch (Exception redisException) {
            log.error(">>> [REDIS ERROR] Không thể kết nối Redis: {}. Skip iteration này.", redisException.getMessage());
            if (songIdStr != null && lockKey == null) {
                try {
                    redisTemplate.opsForList().leftPush(RedisKeys.TRANSCODE_QUEUE, songIdStr);
                } catch (Exception e) {
                    log.error("Không thể push lại vào queue: {}", e.getMessage());
                }
            }
            return;
        }

        log.info(">>> [WORKER] Bắt đầu Transcode bài hát ID: {}", songIdStr);

        File tempInDir = null;
        File tempOutDir = null;
        UUID songId = UUID.fromString(songIdStr);
        long startTime = System.currentTimeMillis();

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

            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
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

            redisTemplate.delete(RedisKeys.TRANSCODE_RETRY_PREFIX + songIdStr);
            
            // Track metrics
            long duration = System.currentTimeMillis() - startTime;
            workerMetricsService.trackSuccess(duration);
            
            log.info(">>> [SUCCESS] Transcode xong bài: {} trong {}ms", songId, duration);

        } catch (Exception e) {
            log.error(">>> [ERROR] Lỗi Transcode bài {}: {}", songIdStr, e.getMessage());
            workerMetricsService.trackFailure();
            handleFailure(songIdStr, e.getMessage());
        } finally {
            // Clean up lock với safety check - chỉ xóa nếu lock vẫn thuộc worker này
            if (lockKey != null) {
                try {
                    String lockIdentifier = LOCK_VALUE + ":" + Thread.currentThread().getId();
                    String currentLock = redisTemplate.opsForValue().get(lockKey);
                    if (lockIdentifier.equals(currentLock)) {
                        redisTemplate.delete(lockKey);
                        log.debug("Đã xóa lock cho bài: {}", songIdStr);
                    } else {
                        log.warn("Lock đã thay đổi hoặc hết hạn cho bài: {}", songIdStr);
                    }
                } catch (Exception e) {
                    log.error("Không thể xóa lock key: {}", e.getMessage());
                }
            }
            
            cleanupTempDirectory(tempInDir, "input");
            cleanupTempDirectory(tempOutDir, "output");
        }
    }
    
    private void cleanupTempDirectory(File dir, String type) {
        if (dir != null && dir.exists()) {
            try {
                FileSystemUtils.deleteRecursively(dir);
                log.debug("Đã xóa temp {} directory: {}", type, dir.getAbsolutePath());
            } catch (Exception e) {
                log.error("Không thể xóa temp {} directory {}: {}", type, dir.getAbsolutePath(), e.getMessage());
                // TODO: Có thể thêm scheduled job để cleanup các file cũ > 24h
            }
        }
    }

    private void handleFailure(String songIdStr, String errorMessage) {
        try {
            String retryKey = RedisKeys.TRANSCODE_RETRY_PREFIX + songIdStr;
            Long currentRetry = redisTemplate.opsForValue().increment(retryKey);

            if (currentRetry != null && currentRetry <= MAX_RETRY) {
                log.warn("Retry lần {}/{} cho bài {}", currentRetry, MAX_RETRY, songIdStr);
                redisTemplate.opsForList().leftPush(RedisKeys.TRANSCODE_QUEUE, songIdStr);
                redisTemplate.expire(retryKey, Duration.ofHours(24));
            } else {
                log.error("Đã hết lượt Retry. Đẩy bài {} vào Dead Letter Queue.", songIdStr);
                redisTemplate.opsForList().leftPush(RedisKeys.TRANSCODE_DLQ, songIdStr);
                redisTemplate.delete(retryKey);
                try {
                    updateSongStatus(UUID.fromString(songIdStr), null, SongStatus.TRANSCODE_FAILED);
                } catch (Exception ex) {
                    log.error("Lỗi update status failed cho bài {}: {}", songIdStr, ex.getMessage());
                }
            }
        } catch (Exception redisEx) {
            log.error(">>> [REDIS ERROR] Không thể xử lý failure cho bài {}: {}. Bỏ qua job này.", 
                    songIdStr, redisEx.getMessage());

        }
    }

    @Transactional
    public void updateSongStatus(UUID songId, String hlsUrl, SongStatus status) {
        Song song = songRepository.findById(songId).orElseThrow();
        if (hlsUrl != null) song.setAudioUrl(hlsUrl);
        
        if (status == SongStatus.PENDING_APPROVAL && song.getStatus() == SongStatus.PROCESSING) {
            song.setStatus(SongStatus.DRAFT);
            log.info("Song {} transcoded successfully, changed to DRAFT status", songId);
        } else {
            song.setStatus(status);
        }
        
        songRepository.save(song);
    }
}
