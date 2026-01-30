package iuh.fit.se.tramcamxuc.modules.statistic.service;

import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.ChartResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.DashboardStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.RevenueStatsResponse;

public interface StatisticService {
    DashboardStatsResponse getDashboardStats();
    ChartResponse getListeningTrend();
    RevenueStatsResponse getRevenueStats();
}
