package iuh.fit.se.tramcamxuc.modules.playlist.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AddSongRequest {
    @NotNull(message = "ID bài hát không được thiếu")
    private UUID songId;
}