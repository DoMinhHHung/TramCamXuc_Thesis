package iuh.fit.se.tramcamxuc.modules.playlist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePlaylistRequest {
    @NotBlank(message = "Tên playlist không được để trống")
    @Size(max = 150, message = "Tên tối đa 150 ký tự")
    private String name;

    private String description;

    private boolean isPublic = true;
}