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
    private boolean active;
    private LocalDateTime createdAt;

    public static AdResponse fromEntity(Advertisement ad) {
        return AdResponse.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .sponsorName(ad.getSponsorName())
                .clickUrl(ad.getClickUrl())
                .audioUrl(ad.getAudioUrl())
                .active(ad.isActive())
                .createdAt(ad.getCreatedAt())
                .build();
    }
}