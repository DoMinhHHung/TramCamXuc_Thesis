package iuh.fit.se.tramcamxuc.modules.album.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateAlbumRequest {
    @NotBlank(message = "Tên album không được để trống")
    @Size(max = 150, message = "Tên album tối đa 150 ký tự")
    private String title;

    private String description;

    private LocalDateTime releaseDate;

    private List<UUID> songIds;
}