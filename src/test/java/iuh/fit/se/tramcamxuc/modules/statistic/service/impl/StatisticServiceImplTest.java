package iuh.fit.se.tramcamxuc.modules.statistic.service.impl;

import iuh.fit.se.tramcamxuc.modules.artist.entity.Artist;
import iuh.fit.se.tramcamxuc.modules.artist.repository.ArtistRepository;
import iuh.fit.se.tramcamxuc.modules.genre.entity.Genre;
import iuh.fit.se.tramcamxuc.modules.payment.repository.PaymentTransactionRepository;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import iuh.fit.se.tramcamxuc.modules.song.repository.SongRepository;
import iuh.fit.se.tramcamxuc.modules.statistic.document.ListenHistoryDoc;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.ChartResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.DashboardStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.dto.response.RevenueStatsResponse;
import iuh.fit.se.tramcamxuc.modules.statistic.repository.ListenHistoryMongoRepository;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.enums.SubscriptionStatus;
import iuh.fit.se.tramcamxuc.modules.subscription.repository.UserSubscriptionRepository;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.repository.UserRepository;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ListenHistoryMongoRepository historyRepository;

    @Mock
    private PaymentTransactionRepository paymentRepository;

    @Mock
    private UserSubscriptionRepository subscriptionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private StatisticServiceImpl statisticService;

    private User user;
    private Song song;
    private Artist artist;
    private Genre genre;
    private UUID userId;
    private UUID songId;
    private UUID artistId;
    private UUID genreId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        songId = UUID.randomUUID();
        artistId = UUID.randomUUID();
        genreId = UUID.randomUUID();

        user = User.builder()
                .email("user@test.com")
                .fullName("Test User")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        artist = Artist.builder()
                .artistName("Test Artist")
                .user(user)
                .build();
        ReflectionTestUtils.setField(artist, "id", artistId);

        genre = Genre.builder()
                .name("Pop")
                .slug("pop")
                .build();
        ReflectionTestUtils.setField(genre, "id", genreId);

        song = Song.builder()
                .title("Test Song")
                .artist(artist)
                .genre(genre)
                .status(SongStatus.PUBLIC)
                .build();
        ReflectionTestUtils.setField(song, "id", songId);
        ReflectionTestUtils.setField(song, "createdAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("Should get dashboard stats successfully")
    void getDashboardStats_Success() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(songRepository.count()).thenReturn(500L);
        when(artistRepository.count()).thenReturn(50L);
        when(historyRepository.count()).thenReturn(10000L);

        // When
        DashboardStatsResponse result = statisticService.getDashboardStats();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getTotalUsers());
        assertEquals(500L, result.getTotalSongs());
        assertEquals(50L, result.getTotalArtists());
        assertEquals(10000L, result.getTotalPlays());
    }

    @Test
    @DisplayName("Should get listening trend successfully")
    void getListeningTrend_Success() {
        // Given
        when(historyRepository.countByListenedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(100L);

        // When
        ChartResponse result = statisticService.getListeningTrend();

        // Then
        assertNotNull(result);
        assertNotNull(result.getLabels());
        assertNotNull(result.getData());
        assertEquals(7, result.getLabels().size());
        assertEquals(7, result.getData().size());
    }

    @Test
    @DisplayName("Should get revenue stats successfully")
    void getRevenueStats_Success() {
        // Given
        List<Object[]> revenueData = new ArrayList<>();
        revenueData.add(new Object[]{"Premium", 1000000L});
        revenueData.add(new Object[]{"Basic", 500000L});

        when(paymentRepository.getTotalRevenue()).thenReturn(1500000L);
        when(subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE)).thenReturn(100L);
        when(subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED)).thenReturn(10L);
        when(subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED)).thenReturn(20L);
        when(paymentRepository.getRevenueByPlan()).thenReturn(revenueData);

        // When
        RevenueStatsResponse result = statisticService.getRevenueStats();

        // Then
        assertNotNull(result);
        assertEquals(1500000L, result.getTotalRevenue());
        assertEquals(100L, result.getActiveSubscribers());
        assertTrue(result.getChurnRate() > 0);
        assertEquals("Premium", result.getTopPlanName());
        assertEquals(1000000L, result.getTopPlanRevenue());
    }

    @Test
    @DisplayName("Should handle zero churn rate when no subscriptions")
    void getRevenueStats_ZeroChurnRate() {
        // Given
        when(paymentRepository.getTotalRevenue()).thenReturn(0L);
        when(subscriptionRepository.countByStatus(any())).thenReturn(0L);
        when(paymentRepository.getRevenueByPlan()).thenReturn(Collections.emptyList());

        // When
        RevenueStatsResponse result = statisticService.getRevenueStats();

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getChurnRate());
    }

    @Test
    @DisplayName("Should record listen history successfully")
    void recordListenHistory_Success() {
        // Given
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));
        when(historyRepository.save(any(ListenHistoryDoc.class))).thenReturn(null);

        // When
        statisticService.recordListenHistory(userId, songId);

        // Then
        verify(historyRepository).save(any(ListenHistoryDoc.class));
    }

    @Test
    @DisplayName("Should handle recording listen history when song not found")
    void recordListenHistory_SongNotFound() {
        // Given
        when(songRepository.findById(songId)).thenReturn(Optional.empty());

        // When
        statisticService.recordListenHistory(userId, songId);

        // Then
        verify(historyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get listen history with pagination")
    void getListenHistory_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        ListenHistoryDoc historyDoc = ListenHistoryDoc.builder()
                .songId(songId)
                .userId(userId)
                .build();
        Page<ListenHistoryDoc> historyPage = new PageImpl<>(Collections.singletonList(historyDoc));

        when(userService.getCurrentUser()).thenReturn(user);
        when(historyRepository.findByUserIdOrderByListenedAtDesc(any(), any())).thenReturn(historyPage);
        when(songRepository.findById(songId)).thenReturn(Optional.of(song));

        // When
        Page<SongResponse> result = statisticService.getListenHistory(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should handle empty revenue data")
    void getRevenueStats_EmptyRevenueData() {
        // Given
        when(paymentRepository.getTotalRevenue()).thenReturn(0L);
        when(subscriptionRepository.countByStatus(any())).thenReturn(0L);
        when(paymentRepository.getRevenueByPlan()).thenReturn(null);

        // When
        RevenueStatsResponse result = statisticService.getRevenueStats();

        // Then
        assertNotNull(result);
        assertEquals("N/A", result.getTopPlanName());
        assertEquals(0L, result.getTopPlanRevenue());
    }
}
