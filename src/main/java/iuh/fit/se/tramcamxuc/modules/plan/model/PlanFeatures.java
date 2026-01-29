package iuh.fit.se.tramcamxuc.modules.plan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFeatures implements Serializable {
    private boolean canBecomeArtist;
    private int maxUploadSongs;
    private boolean canDownload;
    private int maxDownloadSongs;
    private boolean removeAds;
}