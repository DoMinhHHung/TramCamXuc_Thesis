package iuh.fit.se.tramcamxuc.modules.playlist.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePlaylistRequest {
    @Size(max = 150)
    private String name;

    private String description;

    private Boolean isPublic;
}