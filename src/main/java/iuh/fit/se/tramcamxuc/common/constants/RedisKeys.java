package iuh.fit.se.tramcamxuc.common.constants;

/**
 * Redis key constants - Centralized management
 */
public final class RedisKeys {
    
    // Transcode Worker Keys
    public static final String TRANSCODE_QUEUE = "music:transcode:queue";
    public static final String TRANSCODE_DLQ = "music:transcode:dead";
    public static final String TRANSCODE_LOCK_PREFIX = "LOCK:TRANSCODE_WORKER:";
    public static final String TRANSCODE_RETRY_PREFIX = "music:transcode:retry:";
    public static final String TRANSCODE_METRICS = "music:transcode:metrics";
    
    // Rate Limit Keys
    public static final String RATE_LIMIT_PREFIX = "RATE_LIMIT:";
    
    // Cache Keys
    public static final String CACHE_TOP5_TRENDING = "top5Trending";
    public static final String CACHE_ALL_GENRES = "all_genres";
    
    private RedisKeys() {
        // Prevent instantiation
    }
}
