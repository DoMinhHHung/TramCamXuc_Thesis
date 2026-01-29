package iuh.fit.se.tramcamxuc.modules.plan.dto.response;

import iuh.fit.se.tramcamxuc.modules.plan.entity.SubscriptionPlan;
import iuh.fit.se.tramcamxuc.modules.plan.model.PlanFeatures;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanResponse {
    private String id;
    private String name;
    private String description;
    private int price;
    private int durationDays;
    private boolean isActive;
    private PlanFeatures features;

    public static PlanResponse fromEntity(SubscriptionPlan plan) {
        return PlanResponse.builder()
                .id(plan.getId().toString())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .isActive(plan.isActive())
                .features(plan.getFeatures())
                .build();
    }
}