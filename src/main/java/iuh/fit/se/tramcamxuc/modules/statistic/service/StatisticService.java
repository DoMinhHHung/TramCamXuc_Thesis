package iuh.fit.se.tramcamxuc.modules.statistic.service;

import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.ChartResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.DashboardStatsResponse;

public interface StatisticService {
    DashboardStatsResponse getDashboardStats();
    ChartResponse getListeningTrend();
}
