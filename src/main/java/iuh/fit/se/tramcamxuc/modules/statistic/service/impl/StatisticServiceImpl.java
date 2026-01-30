package iuh.fit.se.tramcamxuc.modules.statistic.service.impl;

import iuh.fit.se.tramcamxuc.modules.artist.repository.ArtistRepository;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.ChartResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.DashboardStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.repository.ListenHistoryRepository;
import iuh.fit.se.tramcamxuc.modules.statistic.service.StatisticService;
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
}
