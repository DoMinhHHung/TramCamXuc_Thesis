package iuh.fit.se.tramcamxuc.modules.album.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateAlbumRequest {
    @Size(max = 150, message = "Tên album tối đa 150 ký tự")
    private String title;

    private String description;

    private LocalDateTime releaseDate;
}