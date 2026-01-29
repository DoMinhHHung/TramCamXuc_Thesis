package iuh.fit.se.tramcamxuc.modules.artist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterArtistRequest {
    @NotBlank(message = "Nghệ danh không được để trống")
    @Size(min = 2, max = 100, message = "Nghệ danh phải từ 2-100 ký tự")
    private String artistName;

    @Size(max = 1000, message = "Tiểu sử tối đa 1000 ký tự")
    private String bio;

    // Link mạng xã hội (Optional)
    private String facebookUrl;
    private String instagramUrl;
    private String youtubeUrl;
}