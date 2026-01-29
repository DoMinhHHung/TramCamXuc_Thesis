package iuh.fit.se.tramcamxuc.modules.plan.entity;

import iuh.fit.se.tramcamxuc.BaseEntity;
import iuh.fit.se.tramcamxuc.modules.plan.entity.enums.SubscriptionStatus;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private String transactionId;
}