package iuh.fit.se.tramcamxuc.modules.song.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateSongMetadataRequest {
    @NotBlank(message = "Tên bài hát không được để trống")
    private String title;

    private String bio;

    @NotNull(message = "Phải chọn thể loại")
    private UUID genreId;
}
