package iuh.fit.se.tramcamxuc.modules.statistic.service.impl;

import iuh.fit.se.tramcamxuc.modules.artist.repository.ArtistRepository;
import iuh.fit.se.tramcamxuc.modules.payment.repository.PaymentTransactionRepository;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.ChartResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.DashboardStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.RevenueStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.repository.ListenHistoryRepository;
import iuh.fit.se.tramcamxuc.modules.statistic.service.StatisticService;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.enums.SubscriptionStatus;
import iuh.fit.se.tramcamxuc.modules.subscription.repository.UserSubscriptionRepository;
import iuh.fit.se.tramcamxuc.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final ListenHistoryRepository listenHistoryRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        // 1. Đếm User (trừ Admin ra nếu muốn, nhưng thôi đếm hết cho oai)
        long totalUsers = userRepository.count();

        // 2. Đếm bài hát
        long totalSongs = songRepository.count();

        // 3. Đếm Artist
        long totalArtists = artistRepository.count();

        // 4. Tổng lượt nghe
        long totalPlays = songRepository.getTotalPlays();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalSongs(totalSongs)
                .totalArtists(totalArtists)
                .totalPlays(totalPlays)
                .build();
    }

    @Override
    public ChartResponse getListeningTrend() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6);
        List<Object[]> rawData = listenHistoryRepository.getStatsByDate(sevenDaysAgo);

        Map<String, Long> statsMap = new HashMap<>();
        for (Object[] row : rawData) {
            String dateStr = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            statsMap.put(dateStr, count);
        }

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        LocalDate current = LocalDate.now().minusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < 7; i++) {
            String dateKey = current.toString();

            labels.add(current.format(DateTimeFormatter.ofPattern("dd/MM")));
            data.add(statsMap.getOrDefault(dateKey, 0L));

            current = current.plusDays(1);
        }

        return ChartResponse.builder()
                .labels(labels)
                .data(data)
                .build();
    }

    @Override
    public RevenueStatsResponse getRevenueStats() {
        long totalRevenue = paymentRepository.getTotalRevenue();

        long activeSubscribers = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);

        long cancelled = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);
        long expired = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);
        long totalSubs = activeSubscribers + cancelled + expired;

        double churnRate = 0;
        if (totalSubs > 0) {
            churnRate = (double) (cancelled + expired) / totalSubs * 100;
        }

        List<Object[]> revenueData = paymentRepository.getRevenueByPlan();
        Map<String, Long> revenueByPlanMap = new HashMap<>();

        String topPlanName = "N/A";
        long maxRevenue = 0;

        for (Object[] row : revenueData) {
            String planName = (String) row[0];
            Long amount = ((Number) row[1]).longValue();
            revenueByPlanMap.put(planName, amount);

            if (amount > maxRevenue) {
                maxRevenue = amount;
                topPlanName = planName;
            }
        }

        return RevenueStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .activeSubscribers(activeSubscribers)
                .churnRate(Math.round(churnRate * 100.0) / 100.0)
                .revenueByPlan(revenueByPlanMap)
                .topPlanName(topPlanName)
                .topPlanRevenue(maxRevenue)
                .build();
    }
}
