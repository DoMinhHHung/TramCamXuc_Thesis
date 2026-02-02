package iuh.fit.se.tramcamxuc.modules.statistic.service;

import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.ChartResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.DashboardStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.RevenueStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StatisticService {
    DashboardStatsResponse getDashboardStats();
    ChartResponse getListeningTrend();
    RevenueStatsResponse getRevenueStats();

    void recordListenHistory(UUID userId, UUID songId);
    Page<SongResponse> getListenHistory(Pageable pageable);
}