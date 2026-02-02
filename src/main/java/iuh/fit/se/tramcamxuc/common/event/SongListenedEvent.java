package iuh.fit.se.tramcamxuc.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class SongListenedEvent {
    private final UUID songId;
    private final UUID userId; // null if guest user
}
