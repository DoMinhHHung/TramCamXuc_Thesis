package iuh.fit.se.tramcamxuc.modules.plan.repository;

import iuh.fit.se.tramcamxuc.modules.plan.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);

    List<SubscriptionPlan> findByIsActiveTrue();
}