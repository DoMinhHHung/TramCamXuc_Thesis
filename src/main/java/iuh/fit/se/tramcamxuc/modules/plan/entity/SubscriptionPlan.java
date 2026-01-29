package iuh.fit.se.tramcamxuc.modules.plan.entity;

import iuh.fit.se.tramcamxuc.BaseEntity;
import iuh.fit.se.tramcamxuc.modules.plan.converter.PlanFeaturesConverter;
import iuh.fit.se.tramcamxuc.modules.plan.model.PlanFeatures;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = PlanFeaturesConverter.class)
    private PlanFeatures features;
}