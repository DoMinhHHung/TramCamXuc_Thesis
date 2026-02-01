package iuh.fit.se.tramcamxuc.modules.advertisement.service;

import iuh.fit.se.tramcamxuc.modules.advertisement.dto.request.UpdateAdRequest;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.request.UploadAdRequest;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.response.AdGrowthResponse;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.response.AdResponse;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.response.AdStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdvertisementService {
    AdResponse uploadAdvertisement(UploadAdRequest request);
    AdResponse getRandomAdvertisement();
    
    // Admin management
    Page<AdResponse> getAllAds(String keyword, Boolean active, Pageable pageable);
    AdResponse getAdById(UUID id);
    AdResponse updateAd(UUID id, UpdateAdRequest request);
    void deleteAd(UUID id);
    AdResponse toggleAdStatus(UUID id);
    
    // Statistics
    AdStatisticsResponse getStatistics();
    AdGrowthResponse getGrowthTrend();
    
    // Tracking
    void recordImpression(UUID adId);
    void recordClick(UUID adId);
}