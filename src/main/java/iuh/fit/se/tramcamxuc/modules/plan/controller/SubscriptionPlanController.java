package iuh.fit.se.tramcamxuc.modules.plan.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.plan.dto.request.CreatePlanRequest;
import iuh.fit.se.tramcamxuc.modules.plan.dto.response.PlanResponse;
import iuh.fit.se.tramcamxuc.modules.plan.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService planService;

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getPublicPlans() {
        return ResponseEntity.ok(ApiResponse.success(planService.getAllPlans(true)));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getAllPlansForAdmin() {
        return ResponseEntity.ok(ApiResponse.success(planService.getAllPlans(false)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlanResponse>> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(planService.createPlan(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlanResponse>> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(planService.updatePlan(id, request)));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> toggleStatus(@PathVariable UUID id) {
        planService.togglePlanStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái gói cước thành công"));
    }
}