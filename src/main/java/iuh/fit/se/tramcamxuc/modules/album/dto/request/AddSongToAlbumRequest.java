package iuh.fit.se.tramcamxuc.modules.album.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class AddSongToAlbumRequest {
    @NotEmpty(message = "Danh sách bài hát không được rỗng")
    private List<UUID> songIds;
}