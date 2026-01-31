package iuh.fit.se.tramcamxuc.modules.song.entity;

import iuh.fit.se.tramcamxuc.BaseEntity;
import iuh.fit.se.tramcamxuc.common.utils.SlugUtils;
import iuh.fit.se.tramcamxuc.modules.artist.entity.Artist;
import iuh.fit.se.tramcamxuc.modules.genre.entity.Genre;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "songs", indexes = {
        @Index(name = "idx_song_artist", columnList = "artist_id"),
        @Index(name = "idx_song_genre", columnList = "genre_id"),
        @Index(name = "idx_song_status", columnList = "status"),
        @Index(name = "idx_song_play_count", columnList = "playCount"),
        @Index(name = "idx_song_slug", columnList = "slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String rawUrl;
    private String audioUrl;

    private String coverUrl;

    private int duration;

    @Enumerated(EnumType.STRING)
    private SongStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @Column(columnDefinition = "bigint default 0")
    private long playCount;

    @PrePersist
    @PreUpdate
    public void generateSlug() {
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = SlugUtils.generateSlug(this.title);
        }
    }
}