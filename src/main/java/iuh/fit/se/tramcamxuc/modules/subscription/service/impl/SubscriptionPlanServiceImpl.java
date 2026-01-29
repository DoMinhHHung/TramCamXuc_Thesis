package iuh.fit.se.tramcamxuc.modules.subscription.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.modules.subscription.dto.request.CreatePlanRequest;
import iuh.fit.se.tramcamxuc.modules.subscription.dto.response.PlanResponse;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.SubscriptionPlan;
import iuh.fit.se.tramcamxuc.modules.subscription.repository.SubscriptionPlanRepository;
import iuh.fit.se.tramcamxuc.modules.subscription.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;

    @Override
    public List<PlanResponse> getAllPlans(boolean onlyActive) {
        List<SubscriptionPlan> plans = onlyActive ?
                planRepository.findByIsActiveTrue() :
                planRepository.findAll();

        return plans.stream()
                .map(PlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        if (planRepository.existsByName(request.getName())) {
            throw new AppException("Subscription plan name already exists");
        }

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .features(request.getFeatures())
                .isActive(true)
                .build();

        return PlanResponse.fromEntity(planRepository.save(plan));
    }

    @Override
    @Transactional
    public PlanResponse updatePlan(UUID id, CreatePlanRequest request) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        if (planRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException("Subscription plan name already exists");
        }

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPrice(request.getPrice());
        plan.setDurationDays(request.getDurationDays());
        plan.setFeatures(request.getFeatures());

        return PlanResponse.fromEntity(planRepository.save(plan));
    }

    @Override
    @Transactional
    public void togglePlanStatus(UUID id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        plan.setActive(!plan.isActive());
        planRepository.save(plan);
    }
}