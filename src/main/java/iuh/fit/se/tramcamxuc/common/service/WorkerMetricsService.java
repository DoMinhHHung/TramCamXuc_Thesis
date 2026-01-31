package iuh.fit.se.tramcamxuc.common.service;

import iuh.fit.se.tramcamxuc.common.constants.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Service for tracking worker metrics in Redis
 * Separated for better maintainability
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerMetricsService {
    
    private final StringRedisTemplate redisTemplate;
    
    public void trackSuccess(long durationMs) {
        try {
            redisTemplate.opsForHash().increment(RedisKeys.TRANSCODE_METRICS, "success_count", 1);
            redisTemplate.opsForHash().increment(RedisKeys.TRANSCODE_METRICS, "total_duration_ms", durationMs);
            redisTemplate.expire(RedisKeys.TRANSCODE_METRICS, Duration.ofDays(7));
        } catch (Exception e) {
            log.debug("Không thể ghi success metrics: {}", e.getMessage());
        }
    }
    
    public void trackFailure() {
        try {
            redisTemplate.opsForHash().increment(RedisKeys.TRANSCODE_METRICS, "failure_count", 1);
            redisTemplate.expire(RedisKeys.TRANSCODE_METRICS, Duration.ofDays(7));
        } catch (Exception e) {
            log.debug("Không thể ghi failure metrics: {}", e.getMessage());
        }
    }
    
    public Map<String, Object> getMetrics(String workerIdentifier) {
        try {
            Map<Object, Object> metrics = redisTemplate.opsForHash().entries(RedisKeys.TRANSCODE_METRICS);
            Long queueSize = redisTemplate.opsForList().size(RedisKeys.TRANSCODE_QUEUE);
            Long dlqSize = redisTemplate.opsForList().size(RedisKeys.TRANSCODE_DLQ);
            
            return Map.of(
                "success_count", metrics.getOrDefault("success_count", "0"),
                "failure_count", metrics.getOrDefault("failure_count", "0"),
                "total_duration_ms", metrics.getOrDefault("total_duration_ms", "0"),
                "queue_size", queueSize != null ? queueSize : 0,
                "dlq_size", dlqSize != null ? dlqSize : 0,
                "worker_id", workerIdentifier
            );
        } catch (Exception e) {
            log.error("Không thể lấy metrics: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
}
