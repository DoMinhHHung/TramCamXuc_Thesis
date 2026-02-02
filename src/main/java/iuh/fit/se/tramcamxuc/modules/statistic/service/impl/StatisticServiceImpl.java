package iuh.fit.se.tramcamxuc.modules.statistic.service.impl;

import iuh.fit.se.tramcamxuc.modules.artist.repository.ArtistRepository;
import iuh.fit.se.tramcamxuc.modules.payment.repository.PaymentTransactionRepository;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import iuh.fit.se.tramcamxuc.modules.statistic.document.ListenHistoryDoc;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.ChartResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.DashboardStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.RevenueStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.repository.ListenHistoryMongoRepository;
import iuh.fit.se.tramcamxuc.modules.statistic.service.StatisticService;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.enums.SubscriptionStatus;
import iuh.fit.se.tramcamxuc.modules.subscription.repository.UserSubscriptionRepository;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.repository.UserRepository;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticServiceImpl implements StatisticService {
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final ListenHistoryMongoRepository historyRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final UserService userService;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();

        long totalSongs = songRepository.count();

        long totalArtists = artistRepository.count();

        long totalPlays = historyRepository.count();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalSongs(totalSongs)
                .totalArtists(totalArtists)
                .totalPlays(totalPlays)
                .build();
    }

    @Override
    public ChartResponse getListeningTrend() {
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        
        LocalDate current = LocalDate.now().minusDays(6);
        
        for (int i = 0; i < 7; i++) {
            LocalDateTime startOfDay = current.atStartOfDay();
            LocalDateTime endOfDay = current.plusDays(1).atStartOfDay();
            
            long count = historyRepository.countByListenedAtBetween(startOfDay, endOfDay);
            
            labels.add(current.format(DateTimeFormatter.ofPattern("dd/MM")));
            data.add(count);
            
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

        if (revenueData != null && !revenueData.isEmpty()) {
            for (Object[] row : revenueData) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    String planName = (String) row[0];
                    Long amount = ((Number) row[1]).longValue();
                    revenueByPlanMap.put(planName, amount);

                    if (amount > maxRevenue) {
                        maxRevenue = amount;
                        topPlanName = planName;
                    }
                }
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

    @Override
    @Async("taskExecutor")
    public void recordListenHistory(UUID userId, UUID songId) {
        Song song = songRepository.findById(songId).orElse(null);
        if (song == null) {
            log.warn("Song not found for listen history: {}", songId);
            return;
        }

        ListenHistoryDoc doc = ListenHistoryDoc.builder()
                .userId(userId)
                .songId(songId)
                .songTitle(song.getTitle())
                .artistNames(song.getArtist().getArtistName())
                .coverUrl(song.getCoverUrl())
                .genreId(song.getGenre().getId())
                .listenedAt(LocalDateTime.now())
                .build();

        historyRepository.save(doc);
        log.debug("Recorded listen history for user {} - song {}", userId, songId);
    }

    @Override
    public Page<SongResponse> getListenHistory(Pageable pageable) {
        User user = userService.getCurrentUser();

        return historyRepository.findByUserIdOrderByListenedAtDesc(user.getId(), pageable)
                .map(doc -> {
                    Song song = songRepository.findById(doc.getSongId()).orElse(null);
                    if (song != null) {
                        return SongResponse.fromEntity(song);
                    }
                    
                    return SongResponse.builder()
                            .id(doc.getSongId().toString())
                            .title(doc.getSongTitle())
                            .artistName(doc.getArtistNames())
                            .coverUrl(doc.getCoverUrl())
                            .build();
                });
    }
}
