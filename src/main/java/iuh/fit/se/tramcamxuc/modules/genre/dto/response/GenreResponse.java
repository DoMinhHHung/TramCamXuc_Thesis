package iuh.fit.se.tramcamxuc.modules.genre.dto.response;

import iuh.fit.se.tramcamxuc.modules.genre.entity.Genre;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenreResponse {
    private String id;
    private String name;

    public static GenreResponse fromEntity(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId().toString())
                .name(genre.getName())
                .build();
    }
}