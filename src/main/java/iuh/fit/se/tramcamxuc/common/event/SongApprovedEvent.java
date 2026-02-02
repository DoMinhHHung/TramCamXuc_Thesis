package iuh.fit.se.tramcamxuc.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class SongApprovedEvent {
    private final UUID songId;
    private final String artistEmail;
    private final String artistName;
    private final String songTitle;
    private final String songSlug;
}
