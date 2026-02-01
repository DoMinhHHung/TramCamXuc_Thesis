package iuh.fit.se.tramcamxuc.modules.song.dto.response;

import iuh.fit.se.tramcamxuc.modules.advertisement.dto.response.AdResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SongWithAdResponse {
    private SongResponse song;
    private boolean shouldShowAd;
    private AdResponse advertisement; // null nếu shouldShowAd = false
}
