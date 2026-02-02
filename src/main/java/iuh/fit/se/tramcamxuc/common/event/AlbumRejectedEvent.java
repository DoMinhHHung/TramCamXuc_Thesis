package iuh.fit.se.tramcamxuc.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AlbumRejectedEvent {
    private final UUID albumId;
    private final String artistEmail;
    private final String artistName;
    private final String albumTitle;
    private final String albumCover;
    private final int songCount;
    private final String submittedDate;
    private final String reason;
    private final String albumIdStr;
}
