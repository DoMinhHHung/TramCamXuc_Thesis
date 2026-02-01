package iuh.fit.se.tramcamxuc.modules.advertisement.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdStatisticsResponse {
    private long totalAds;
    private long activeAds;
    private long inactiveAds;
    private long totalImpressions;
    private long totalClicks;
    private double clickThroughRate;
}
