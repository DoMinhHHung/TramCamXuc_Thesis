package iuh.fit.se.tramcamxuc.modules.artist.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateArtistRequest {
    @Size(max = 1000, message = "Tiểu sử tối đa 1000 ký tự")
    private String bio;

    private String facebookUrl;
    private String instagramUrl;
    private String youtubeUrl;

    private String artistName;
}