package iuh.fit.se.tramcamxuc.modules.plan.service;

import iuh.fit.se.tramcamxuc.modules.plan.dto.request.CreatePlanRequest;
import iuh.fit.se.tramcamxuc.modules.plan.dto.response.PlanResponse;
import java.util.List;
import java.util.UUID;

public interface SubscriptionPlanService {
    List<PlanResponse> getAllPlans(boolean onlyActive);
    PlanResponse createPlan(CreatePlanRequest request);
    PlanResponse updatePlan(UUID id, CreatePlanRequest request);
    void togglePlanStatus(UUID id);
}