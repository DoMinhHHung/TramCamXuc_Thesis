package iuh.fit.se.tramcamxuc.common.worker;

import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Scheduler để sync play count từ Redis xuống Database
 * Write-Behind Pattern: Giảm write load lên DB bằng cách batch update
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlayCountSyncWorker {

    private final StringRedisTemplate redisTemplate;
    private final SongRepository songRepository;

    /**
     * Chạy mỗi 5 phút để sync play count từ Redis về DB
     * - Scan tất cả keys "song_view:*"
     * - Aggregate counts
     * - Batch update DB
     * - Clear Redis counters sau khi sync thành công
     */
    @Scheduled(fixedDelay = 300000) // 5 phút = 300000ms
    @SchedulerLock(name = "playCountSync", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    @Transactional
    @CacheEvict(value = "top5Trending", allEntries = true) // Invalidate cache sau khi sync
    public void syncPlayCountToDatabase() {
        long startTime = System.currentTimeMillis();
        int totalSynced = 0;
        int totalErrors = 0;

        try {
            log.info(">>> [SYNC] Bắt đầu sync play count từ Redis về DB");

            Map<UUID, Long> songViewCounts = new HashMap<>();

            // Scan tất cả keys song_view:* trong Redis
            ScanOptions scanOptions = ScanOptions.scanOptions()
                    .match("song_view:*")
                    .count(100) // Scan 100 keys mỗi lần
                    .build();

            Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .scan(scanOptions);

            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                try {
                    // Extract songId từ key "song_view:{uuid}"
                    String songIdStr = key.substring("song_view:".length());
                    UUID songId = UUID.fromString(songIdStr);

                    // Get count từ Redis
                    String countStr = redisTemplate.opsForValue().get(key);
                    if (countStr != null) {
                        long count = Long.parseLong(countStr);
                        if (count > 0) {
                            songViewCounts.put(songId, count);
                        }
                    }
                } catch (Exception e) {
                    log.error("Lỗi parse key Redis: {} - {}", key, e.getMessage());
                    totalErrors++;
                }
            }

            cursor.close();

            // Batch update DB
            if (!songViewCounts.isEmpty()) {
                log.info("Tìm thấy {} bài hát cần sync play count", songViewCounts.size());

                for (Map.Entry<UUID, Long> entry : songViewCounts.entrySet()) {
                    UUID songId = entry.getKey();
                    Long count = entry.getValue();

                    try {
                        // Update play count trong DB
                        int updated = songRepository.incrementPlayCountBy(songId, count.intValue());
                        
                        if (updated > 0) {
                            // Xóa Redis counter sau khi sync thành công
                            redisTemplate.delete("song_view:" + songId);
                            totalSynced++;
                            log.debug("Synced song {} với {} views", songId, count);
                        } else {
                            log.warn("Không tìm thấy song {} để update", songId);
                        }
                    } catch (Exception e) {
                        log.error("Lỗi sync song {}: {}", songId, e.getMessage());
                        totalErrors++;
                    }
                }

                long duration = System.currentTimeMillis() - startTime;
                log.info(">>> [SYNC SUCCESS] Đã sync {} songs trong {}ms. Errors: {}", 
                         totalSynced, duration, totalErrors);
            } else {
                log.info(">>> [SYNC] Không có play count nào cần sync");
            }

        } catch (Exception e) {
            log.error(">>> [SYNC ERROR] Lỗi sync play count: {}", e.getMessage(), e);
        }
    }

    /**
     * Method để force sync ngay lập tức (có thể gọi từ admin endpoint)
     */
    public Map<String, Object> forceSyncNow() {
        long beforeSync = System.currentTimeMillis();
        syncPlayCountToDatabase();
        long syncDuration = System.currentTimeMillis() - beforeSync;

        return Map.of(
            "message", "Force sync completed",
            "duration_ms", syncDuration,
            "timestamp", System.currentTimeMillis()
        );
    }
}
