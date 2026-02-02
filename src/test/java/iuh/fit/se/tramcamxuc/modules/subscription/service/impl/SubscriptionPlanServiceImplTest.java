package iuh.fit.se.tramcamxuc.modules.subscription.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.modules.subscription.dto.request.CreatePlanRequest;
import iuh.fit.se.tramcamxuc.modules.subscription.dto.response.PlanResponse;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.SubscriptionPlan;
import iuh.fit.se.tramcamxuc.modules.subscription.model.PlanFeatures;
import iuh.fit.se.tramcamxuc.modules.subscription.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceImplTest {

    @Mock
    private SubscriptionPlanRepository planRepository;

    @InjectMocks
    private SubscriptionPlanServiceImpl subscriptionPlanService;

    private SubscriptionPlan plan;
    private PlanFeatures features;
    private UUID planId;

    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();

        features = new PlanFeatures();
        features.setCanBecomeArtist(true);
        features.setMaxUploadSongs(100);

        plan = SubscriptionPlan.builder()
                .name("Premium Plan")
                .description("Premium features")
                .price(99000)
                .durationDays(30)
                .features(features)
                .isActive(true)
                .build();
        ReflectionTestUtils.setField(plan, "id", planId);
    }

    @Test
    @DisplayName("Should get all active plans successfully")
    void getAllPlans_OnlyActive() {
        // Given
        when(planRepository.findByIsActiveTrue()).thenReturn(Collections.singletonList(plan));

        // When
        List<PlanResponse> result = subscriptionPlanService.getAllPlans(true);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(planRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Should get all plans including inactive")
    void getAllPlans_IncludingInactive() {
        // Given
        when(planRepository.findAll()).thenReturn(Collections.singletonList(plan));

        // When
        List<PlanResponse> result = subscriptionPlanService.getAllPlans(false);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(planRepository).findAll();
    }

    @Test
    @DisplayName("Should create plan successfully")
    void createPlan_Success() {
        // Given
        CreatePlanRequest request = new CreatePlanRequest();
        request.setName("New Plan");
        request.setDescription("New Description");
        request.setPrice(49000);
        request.setDurationDays(30);
        request.setFeatures(features);

        when(planRepository.existsByName("New Plan")).thenReturn(false);
        when(planRepository.save(any(SubscriptionPlan.class))).thenReturn(plan);

        // When
        PlanResponse result = subscriptionPlanService.createPlan(request);

        // Then
        assertNotNull(result);
        verify(planRepository).save(any(SubscriptionPlan.class));
    }

    @Test
    @DisplayName("Should throw exception when creating plan with existing name")
    void createPlan_NameExists() {
        // Given
        CreatePlanRequest request = new CreatePlanRequest();
        request.setName("Existing Plan");

        when(planRepository.existsByName("Existing Plan")).thenReturn(true);

        // When & Then
        assertThrows(AppException.class, () -> 
            subscriptionPlanService.createPlan(request)
        );
    }

    @Test
    @DisplayName("Should update plan successfully")
    void updatePlan_Success() {
        // Given
        CreatePlanRequest request = new CreatePlanRequest();
        request.setName("Updated Plan");
        request.setDescription("Updated Description");
        request.setPrice(79000);
        request.setDurationDays(30);
        request.setFeatures(features);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepository.existsByNameAndIdNot("Updated Plan", planId)).thenReturn(false);
        when(planRepository.save(any(SubscriptionPlan.class))).thenReturn(plan);

        // When
        PlanResponse result = subscriptionPlanService.updatePlan(planId, request);

        // Then
        assertNotNull(result);
        verify(planRepository).save(any(SubscriptionPlan.class));
    }

    @Test
    @DisplayName("Should throw exception when updating to existing plan name")
    void updatePlan_NameExists() {
        // Given
        CreatePlanRequest request = new CreatePlanRequest();
        request.setName("Existing Plan");

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepository.existsByNameAndIdNot("Existing Plan", planId)).thenReturn(true);

        // When & Then
        assertThrows(AppException.class, () -> 
            subscriptionPlanService.updatePlan(planId, request)
        );
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent plan")
    void updatePlan_PlanNotFound() {
        // Given
        CreatePlanRequest request = new CreatePlanRequest();
        request.setName("Updated Plan");

        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            subscriptionPlanService.updatePlan(planId, request)
        );
    }

    @Test
    @DisplayName("Should toggle plan status successfully")
    void togglePlanStatus_Success() {
        // Given
        boolean originalStatus = plan.isActive();
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        // When
        subscriptionPlanService.togglePlanStatus(planId);

        // Then
        verify(planRepository).save(plan);
        assertNotEquals(originalStatus, plan.isActive());
    }

    @Test
    @DisplayName("Should throw exception when toggling non-existent plan")
    void togglePlanStatus_PlanNotFound() {
        // Given
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            subscriptionPlanService.togglePlanStatus(planId)
        );
    }

    @Test
    @DisplayName("Should return empty list when no active plans")
    void getAllPlans_NoActivePlans() {
        // Given
        when(planRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

        // When
        List<PlanResponse> result = subscriptionPlanService.getAllPlans(true);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
