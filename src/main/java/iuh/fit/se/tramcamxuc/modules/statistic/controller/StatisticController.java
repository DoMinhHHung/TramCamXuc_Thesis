package iuh.fit.se.tramcamxuc.modules.statistic.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.ChartResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.DashboardStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.RevenueStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    // --- ADMIN APIs ---

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(statisticService.getDashboardStats()));
    }

    @GetMapping("/admin/trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChartResponse>> getListeningTrend() {
        return ResponseEntity.ok(ApiResponse.success(statisticService.getListeningTrend()));
    }

    @GetMapping("/admin/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RevenueStatsResponse>> getRevenueStats() {
        return ResponseEntity.ok(ApiResponse.success(statisticService.getRevenueStats()));
    }

    // --- USER APIs ---

    @GetMapping("/history")
    @PreAuthorize("hasRole('USER') or hasRole('ARTIST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<SongResponse>>> getMyListenHistory(
            @PageableDefault(sort = "listened_at", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(statisticService.getListenHistory(pageable)));
    }
}