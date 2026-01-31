package iuh.fit.se.tramcamxuc.modules.subscription.entity;

import iuh.fit.se.tramcamxuc.BaseEntity;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.enums.SubscriptionStatus;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscriptions", indexes = {
        @Index(name = "idx_user_sub_user", columnList = "user_id"),
        @Index(name = "idx_user_sub_status", columnList = "status"),
        @Index(name = "idx_user_sub_order_code", columnList = "orderCode"),
        @Index(name = "idx_user_sub_user_status", columnList = "user_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(unique = true)
    private Long orderCode;
}