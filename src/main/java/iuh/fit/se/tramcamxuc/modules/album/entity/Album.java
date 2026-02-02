package iuh.fit.se.tramcamxuc.modules.album.entity;

import iuh.fit.se.tramcamxuc.BaseEntity;
import iuh.fit.se.tramcamxuc.modules.artist.entity.Artist;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "albums", indexes = {
        @Index(name = "idx_album_slug", columnList = "slug")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@SQLDelete(sql = "UPDATE albums SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Album extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverUrl;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<Song> songs = new ArrayList<>();

    @Column(unique = true, nullable = false)
    private String slug;

    @Transient
    public int getSongCount() {
        return songs == null ? 0 : songs.size();
    }
}