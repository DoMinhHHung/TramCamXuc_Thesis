package iuh.fit.se.tramcamxuc.modules.statistic.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalUsers;
    private long totalSongs;
    private long totalArtists;
    private long totalPlays;
}