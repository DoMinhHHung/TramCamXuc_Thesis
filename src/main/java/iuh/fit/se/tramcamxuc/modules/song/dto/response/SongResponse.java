package iuh.fit.se.tramcamxuc.modules.song.dto.response;

import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SongResponse {
    private String id;
    private String title;
    private String slug;
    private String coverUrl;
    private String audioUrl;
    private int duration;
    private String artistName;
    private String artistId;
    private String genreName;
    private String status;
    private long playCount;
    private String createdAt;

    public static SongResponse fromEntity(Song song) {
        return SongResponse.builder()
                .id(song.getId().toString())
                .title(song.getTitle())
                .slug(song.getSlug())
                .coverUrl(song.getCoverUrl())
                .audioUrl(song.getAudioUrl())
                .duration(song.getDuration())
                .artistName(song.getArtist().getArtistName())
                .artistId(song.getArtist().getId().toString())
                .genreName(song.getGenre().getName())
                .status(song.getStatus().name())
                .playCount(song.getPlayCount())
                .createdAt(song.getCreatedAt().toString())
                .build();
    }
}