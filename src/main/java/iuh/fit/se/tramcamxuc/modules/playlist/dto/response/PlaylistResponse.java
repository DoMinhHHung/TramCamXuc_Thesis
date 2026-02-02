package iuh.fit.se.tramcamxuc.modules.playlist.dto.response;

import iuh.fit.se.tramcamxuc.modules.playlist.entity.Playlist;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class PlaylistResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String thumbnailUrl;
    private boolean isPublic;

    private UUID ownerId;
    private String ownerName;

    private int totalSongs;
    private long totalDuration;
    private List<SongResponse> songs;

    public static PlaylistResponse fromEntity(Playlist playlist) {
        long duration = 0;
        List<SongResponse> songResponses = List.of();

        if (playlist.getPlaylistSongs() != null && !playlist.getPlaylistSongs().isEmpty()) {
            songResponses = playlist.getPlaylistSongs().stream()
                    .map(ps -> SongResponse.fromEntity(ps.getSong()))
                    .collect(Collectors.toList());

            duration = playlist.getPlaylistSongs().stream()
                    .mapToInt(ps -> ps.getSong().getDuration())
                    .sum();
        }

        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .slug(playlist.getSlug())
                .description(playlist.getDescription())
                .thumbnailUrl(playlist.getThumbnailUrl())
                .isPublic(playlist.isPublic())
                .ownerId(playlist.getUser().getId())
                .ownerName(playlist.getUser().getUsername())
                .totalSongs(playlist.getSongCount())
                .totalDuration(duration)
                .songs(songResponses)
                .build();
    }
}