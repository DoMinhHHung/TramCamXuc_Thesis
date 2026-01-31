package iuh.fit.se.tramcamxuc.modules.advertisement.entity;

import iuh.fit.se.tramcamxuc.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "advertisements")
public class Advertisement extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String sponsorName;

    private String clickUrl;

    private String rawUrl;

    private String audioUrl;

    private int duration;

    @Column(columnDefinition = "boolean default true")
    private boolean active;

    private long impressions;
    private long clicks;
}