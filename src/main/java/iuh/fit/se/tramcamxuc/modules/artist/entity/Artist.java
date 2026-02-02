package iuh.fit.se.tramcamxuc.modules.artist.entity;

import iuh.fit.se.tramcamxuc.BaseEntity;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;

@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@BatchSize(size = 20)
public class Artist extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String artistName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String avatarUrl;
    private String coverUrl;

    private String facebookUrl;
    private String instagramUrl;
    private String youtubeUrl;

    @Column(name = "accepted_terms_at")
    private LocalDateTime acceptedTermsAt;

    // Tổng số lượt nghe nhạc của artist này ( sort trending)
    @Column(columnDefinition = "bigint default 0")
    private long totalPlays;
}