package iuh.fit.se.tramcamxuc.modules.artist.dto.response;

import iuh.fit.se.tramcamxuc.modules.artist.entity.Artist;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArtistResponse {
    private String id;
    private String userId;
    private String artistName;
    private String bio;
    private String avatarUrl;
    private String coverUrl;
    private long totalPlays;
    private String facebookUrl;
    private String instagramUrl;
    private String youtubeUrl;

    public static ArtistResponse fromEntity(Artist artist) {
        return ArtistResponse.builder()
                .id(artist.getId().toString())
                .userId(artist.getUser().getId().toString())
                .artistName(artist.getArtistName())
                .bio(artist.getBio())
                .avatarUrl(artist.getAvatarUrl())
                .coverUrl(artist.getCoverUrl())
                .totalPlays(artist.getTotalPlays())
                .facebookUrl(artist.getFacebookUrl())
                .instagramUrl(artist.getInstagramUrl())
                .youtubeUrl(artist.getYoutubeUrl())
                .build();
    }
}