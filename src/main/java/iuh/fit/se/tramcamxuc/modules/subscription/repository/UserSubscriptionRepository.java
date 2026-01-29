package iuh.fit.se.tramcamxuc.modules.subscription.repository;

import iuh.fit.se.tramcamxuc.modules.subscription.entity.UserSubscription;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {
    Optional<UserSubscription> findByOrderCode(Long orderCode);

    boolean existsByUserIdAndStatus(UUID userId, SubscriptionStatus status);
    Optional<UserSubscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);
}