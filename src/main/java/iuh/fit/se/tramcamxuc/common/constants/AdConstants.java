package iuh.fit.se.tramcamxuc.common.constants;

/**
 * Advertisement constants - Configuration for ad display logic
 */
public final class AdConstants {
    
    /**
     * Số bài hát cần phát trước khi hiển thị quảng cáo
     * Đối với FREE user: mỗi X bài hát sẽ có 1 quảng cáo
     * 
     * DEMO MODE: Đặt = 1 để mỗi bài đều có quảng cáo
     * PRODUCTION: Đặt = 3 hoặc 5
     */
    public static final int SONGS_BEFORE_AD = 1; // Thay đổi số này để demo
    
    /**
     * Thời gian giữa các lần hiển thị quảng cáo (phút)
     * Nếu user vừa xem quảng cáo, sẽ không hiển thị lại trong X phút
     * 
     * DEMO MODE: Đặt = 0 hoặc 1 để luôn hiển thị
     * PRODUCTION: Đặt = 30
     */
    public static final int MINUTES_BETWEEN_ADS = 0; // Thay đổi số này để demo
    
    /**
     * Redis key prefix cho tracking số bài hát đã phát
     */
    public static final String USER_SONG_COUNT_PREFIX = "user:song:count:";
    
    /**
     * Redis key prefix cho tracking lần quảng cáo cuối
     */
    public static final String USER_LAST_AD_PREFIX = "user:last:ad:";
    
    private AdConstants() {
        // Prevent instantiation
    }
}
