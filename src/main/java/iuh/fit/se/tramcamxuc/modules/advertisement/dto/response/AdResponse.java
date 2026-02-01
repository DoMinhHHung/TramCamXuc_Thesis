package iuh.fit.se.tramcamxuc.modules.advertisement.dto.response;

import iuh.fit.se.tramcamxuc.modules.advertisement.entity.Advertisement;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AdResponse {
    private UUID id;
    private String title;
    private String sponsorName;
    private String clickUrl;
    private String audioUrl;
    private int duration;
    private boolean active;
    private long impressions;
    private long clicks;
    private double clickThroughRate; // CTR = (clicks / impressions) * 100
    private LocalDateTime createdAt;

    public static AdResponse fromEntity(Advertisement ad) {
        double ctr = ad.getImpressions() > 0 
            ? (double) ad.getClicks() / ad.getImpressions() * 100 
            : 0.0;
        
        return AdResponse.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .sponsorName(ad.getSponsorName())
                .clickUrl(ad.getClickUrl())
                .audioUrl(ad.getAudioUrl())
                .duration(ad.getDuration())
                .active(ad.isActive())
                .impressions(ad.getImpressions())
                .clicks(ad.getClicks())
                .clickThroughRate(Math.round(ctr * 100.0) / 100.0)
                .createdAt(ad.getCreatedAt())
                .build();
    }
}