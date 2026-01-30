package iuh.fit.se.tramcamxuc.modules.statistic.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.ChartResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.DashboardStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(statisticService.getDashboardStats()));
    }

    @GetMapping("/trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChartResponse>> getListeningTrend() {
        return ResponseEntity.ok(ApiResponse.success(statisticService.getListeningTrend()));
    }
}