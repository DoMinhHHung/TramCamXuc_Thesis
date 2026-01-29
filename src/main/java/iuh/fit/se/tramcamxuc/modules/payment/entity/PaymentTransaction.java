package iuh.fit.se.tramcamxuc.modules.payment.entity;

import iuh.fit.se.tramcamxuc.BaseEntity;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.SubscriptionPlan;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction extends BaseEntity {

    @Column(unique = true, nullable = false)
    private Long orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    private int amount;

    @Column(length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String checkoutUrl;
}