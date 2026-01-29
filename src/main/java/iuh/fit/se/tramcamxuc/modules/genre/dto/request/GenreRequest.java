package iuh.fit.se.tramcamxuc.modules.genre.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenreRequest {
    @NotBlank(message = "Tên thể loại không được để trống")
    private String name;

    private String description;
}