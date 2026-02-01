package iuh.fit.se.tramcamxuc.modules.album.dto.response;

import iuh.fit.se.tramcamxuc.modules.album.entity.Album;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class AlbumResponse {
    private UUID id;
    private String title;
    private String description;
    private String coverUrl;
    private LocalDateTime releaseDate;

    private UUID artistId;
    private String artistName;
    private String artistAvatar;

    private int totalSongs;
    private int totalDuration;
    private String slug;
    private List<SongResponse> songs;

    public static AlbumResponse fromEntity(Album album) {
        int totalDuration = 0;
        if (album.getSongs() != null) {
            totalDuration = album.getSongs().stream()
                    .mapToInt(Song::getDuration)
                    .sum();
        }
        return AlbumResponse.builder()
                .id(album.getId())
                .title(album.getTitle())
                .description(album.getDescription())
                .coverUrl(album.getCoverUrl())
                .releaseDate(album.getReleaseDate())
                .artistId(album.getArtist().getId())
                .artistName(album.getArtist().getArtistName())
                .artistAvatar(album.getArtist().getUser().getAvatarUrl())
                .totalSongs(album.getSongs() != null ? album.getSongs().size() : 0)
                .songs(album.getSongs() != null ?
                        album.getSongs().stream()
                                .map(SongResponse::fromEntity)
                                .collect(Collectors.toList())
                        : List.of())
                .totalDuration(totalDuration)
                .slug(album.getSlug())
                .build();
    }
}