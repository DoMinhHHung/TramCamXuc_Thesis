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

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String sponsorName;

    @Column(length = 500)
    private String clickUrl;

    @Column(length = 500)
    private String rawUrl;

    @Column(length = 500)
    private String audioUrl;

    private int duration;

    @Column(columnDefinition = "boolean default true")
    private boolean active;

    @Column(nullable = false)
    private long impressions = 0;
    
    @Column(nullable = false)
    private long clicks = 0;
}