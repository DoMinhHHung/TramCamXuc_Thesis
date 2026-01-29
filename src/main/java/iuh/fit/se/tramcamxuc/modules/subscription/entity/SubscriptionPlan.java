package iuh.fit.se.tramcamxuc.modules.subscription.entity;

import iuh.fit.se.tramcamxuc.BaseEntity;
import iuh.fit.se.tramcamxuc.modules.subscription.converter.PlanFeaturesConverter;
import iuh.fit.se.tramcamxuc.modules.subscription.model.PlanFeatures;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int durationDays;

    private boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = PlanFeaturesConverter.class)
    private PlanFeatures features;
}