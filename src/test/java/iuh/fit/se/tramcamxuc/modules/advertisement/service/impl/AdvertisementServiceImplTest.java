package iuh.fit.se.tramcamxuc.modules.advertisement.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.service.MinioService;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.request.UpdateAdRequest;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.request.UploadAdRequest;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.response.AdGrowthResponse;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.response.AdResponse;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.response.AdStatisticsResponse;
import iuh.fit.se.tramcamxuc.modules.advertisement.entity.Advertisement;
import iuh.fit.se.tramcamxuc.modules.advertisement.repository.AdvertisementRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceImplTest {

    @Mock
    private AdvertisementRepository adRepository;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private AdvertisementServiceImpl advertisementService;

    private Advertisement ad;
    private UUID adId;

    @BeforeEach
    void setUp() {
        adId = UUID.randomUUID();
        ad = Advertisement.builder()
                .title("Test Ad")
                .sponsorName("Test Sponsor")
                .clickUrl("http://example.com")
                .rawUrl("http://minio.com/raw.mp3")
                .audioUrl("http://minio.com/audio.m3u8")
                .duration(30)
                .active(true)
                .impressions(100)
                .clicks(10)
                .build();
    }

    @Test
    @DisplayName("Should get random advertisement successfully")
    void getRandomAdvertisement_Success() {
        // Given
        when(adRepository.findRandomActiveAd()).thenReturn(Optional.of(ad));

        // When
        AdResponse result = advertisementService.getRandomAdvertisement();

        // Then
        assertNotNull(result);
        assertEquals("Test Ad", result.getTitle());
        verify(adRepository).findRandomActiveAd();
    }

    @Test
    @DisplayName("Should return null when no active ad found")
    void getRandomAdvertisement_NoActiveAd() {
        // Given
        when(adRepository.findRandomActiveAd()).thenReturn(Optional.empty());

        // When
        AdResponse result = advertisementService.getRandomAdvertisement();

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should get all ads with pagination")
    void getAllAds_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Advertisement> adPage = new PageImpl<>(Collections.singletonList(ad));
        when(adRepository.searchAds(any(), any(), any())).thenReturn(adPage);

        // When
        Page<AdResponse> result = advertisementService.getAllAds("test", true, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(adRepository).searchAds("test", true, pageable);
    }

    @Test
    @DisplayName("Should get ad by id successfully")
    void getAdById_Success() {
        // Given
        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        // When
        AdResponse result = advertisementService.getAdById(adId);

        // Then
        assertNotNull(result);
        assertEquals("Test Ad", result.getTitle());
        verify(adRepository).findById(adId);
    }

    @Test
    @DisplayName("Should throw exception when ad not found")
    void getAdById_NotFound() {
        // Given
        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            advertisementService.getAdById(adId)
        );
    }

    @Test
    @DisplayName("Should update ad successfully")
    void updateAd_Success() {
        // Given
        UpdateAdRequest request = new UpdateAdRequest();
        request.setTitle("Updated Title");
        request.setSponsorName("Updated Sponsor");
        request.setClickUrl("http://updated.com");

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(adRepository.save(any(Advertisement.class))).thenReturn(ad);

        // When
        AdResponse result = advertisementService.updateAd(adId, request);

        // Then
        assertNotNull(result);
        verify(adRepository).findById(adId);
        verify(adRepository).save(any(Advertisement.class));
    }

    @Test
    @DisplayName("Should delete ad successfully")
    void deleteAd_Success() {
        // Given
        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        // When
        advertisementService.deleteAd(adId);

        // Then
        verify(adRepository).findById(adId);
        verify(adRepository).delete(ad);
    }

    @Test
    @DisplayName("Should toggle ad status successfully")
    void toggleAdStatus_Success() {
        // Given
        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(adRepository.save(any(Advertisement.class))).thenReturn(ad);

        // When
        AdResponse result = advertisementService.toggleAdStatus(adId);

        // Then
        assertNotNull(result);
        verify(adRepository).findById(adId);
        verify(adRepository).save(any(Advertisement.class));
    }

    @Test
    @DisplayName("Should get statistics successfully")
    void getStatistics_Success() {
        // Given
        when(adRepository.count()).thenReturn(10L);
        when(adRepository.countByActive(true)).thenReturn(7L);
        when(adRepository.countByActive(false)).thenReturn(3L);
        when(adRepository.getTotalImpressions()).thenReturn(1000L);
        when(adRepository.getTotalClicks()).thenReturn(100L);

        // When
        AdStatisticsResponse result = advertisementService.getStatistics();

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getTotalAds());
        assertEquals(7L, result.getActiveAds());
        assertEquals(3L, result.getInactiveAds());
        assertEquals(1000L, result.getTotalImpressions());
        assertEquals(100L, result.getTotalClicks());
    }

    @Test
    @DisplayName("Should handle null statistics values")
    void getStatistics_WithNullValues() {
        // Given
        when(adRepository.count()).thenReturn(5L);
        when(adRepository.countByActive(true)).thenReturn(5L);
        when(adRepository.countByActive(false)).thenReturn(0L);
        when(adRepository.getTotalImpressions()).thenReturn(null);
        when(adRepository.getTotalClicks()).thenReturn(null);

        // When
        AdStatisticsResponse result = advertisementService.getStatistics();

        // Then
        assertNotNull(result);
        assertEquals(0L, result.getTotalImpressions());
        assertEquals(0L, result.getTotalClicks());
    }

    @Test
    @DisplayName("Should get growth trend successfully")
    void getGrowthTrend_Success() {
        // Given
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6);
        List<Object[]> rawData = new ArrayList<>();
        rawData.add(new Object[]{sevenDaysAgo.toLocalDate().toString(), 5L});
        
        when(adRepository.getAdsByDate(any(LocalDateTime.class))).thenReturn(rawData);

        // When
        AdGrowthResponse result = advertisementService.getGrowthTrend();

        // Then
        assertNotNull(result);
        assertNotNull(result.getLabels());
        assertNotNull(result.getImpressions());
        assertNotNull(result.getClicks());
        assertEquals(7, result.getLabels().size());
    }

    @Test
    @DisplayName("Should record impression successfully")
    void recordImpression_Success() {
        // Given
        doNothing().when(adRepository).incrementImpressions(adId);

        // When
        advertisementService.recordImpression(adId);

        // Then
        verify(adRepository).incrementImpressions(adId);
    }

    @Test
    @DisplayName("Should record click successfully")
    void recordClick_Success() {
        // Given
        doNothing().when(adRepository).incrementClicks(adId);

        // When
        advertisementService.recordClick(adId);

        // Then
        verify(adRepository).incrementClicks(adId);
    }
}
