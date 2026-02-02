package iuh.fit.se.tramcamxuc.modules.statistic.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Document(collection = "listen_history")
public class ListenHistoryDoc {

    @Id
    private String id;

    @Indexed
    @Field("user_id")
    private UUID userId;

    @Indexed
    @Field("song_id")
    private UUID songId;

    @Field("song_title")
    private String songTitle;

    @Field("artist_names")
    private String artistNames;

    @Field("cover_url")
    private String coverUrl;

    @Field("genre_id")
    private UUID genreId;

    @Indexed(expireAfterSeconds = 31536000)
    @Field("listened_at")
    private LocalDateTime listenedAt;
}