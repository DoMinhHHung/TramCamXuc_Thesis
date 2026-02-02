package iuh.fit.se.tramcamxuc.modules.playlist.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ReorderPlaylistRequest {
    @NotEmpty(message = "Danh sách bài hát mới không được rỗng")
    private List<UUID> songIds;
}