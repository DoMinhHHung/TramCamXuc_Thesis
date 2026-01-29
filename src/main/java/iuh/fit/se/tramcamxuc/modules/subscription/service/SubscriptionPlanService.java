package iuh.fit.se.tramcamxuc.modules.subscription.service;

import iuh.fit.se.tramcamxuc.modules.subscription.dto.request.CreatePlanRequest;
import iuh.fit.se.tramcamxuc.modules.subscription.dto.response.PlanResponse;
import java.util.List;
import java.util.UUID;

public interface SubscriptionPlanService {
    List<PlanResponse> getAllPlans(boolean onlyActive);
    PlanResponse createPlan(CreatePlanRequest request);
    PlanResponse updatePlan(UUID id, CreatePlanRequest request);
    void togglePlanStatus(UUID id);
}