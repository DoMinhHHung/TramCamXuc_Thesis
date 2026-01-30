package iuh.fit.se.tramcamxuc.modules.statistic.entity;

import iuh.fit.se.tramcamxuc.BaseEntity;
import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "listen_history", indexes = {
        @Index(name = "idx_listen_time", columnList = "listenedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListenHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime listenedAt;
}