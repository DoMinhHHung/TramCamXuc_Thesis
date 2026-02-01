package iuh.fit.se.tramcamxuc.modules.admin.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.request.UpdateAdRequest;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.request.UploadAdRequest;
import iuh.fit.se.tramcamxuc.modules.advertisement.service.AdvertisementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/advertisements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAdvertisementController {

    private final AdvertisementService adService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadAd(@ModelAttribute @Valid UploadAdRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Upload quảng cáo thành công")
                .data(adService.uploadAdvertisement(request))
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllAds(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy danh sách quảng cáo thành công")
                .data(adService.getAllAds(keyword, active, pageable))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getAdById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy thông tin quảng cáo thành công")
                .data(adService.getAdById(id))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateAd(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateAdRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Cập nhật quảng cáo thành công")
                .data(adService.updateAd(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAd(@PathVariable UUID id) {
        adService.deleteAd(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Xóa quảng cáo thành công")
                .build());
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse> toggleAdStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Cập nhật trạng thái quảng cáo thành công")
                .data(adService.toggleAdStatus(id))
                .build());
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse> getStatistics() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy thống kê quảng cáo thành công")
                .data(adService.getStatistics())
                .build());
    }

    @GetMapping("/growth")
    public ResponseEntity<ApiResponse> getGrowthTrend() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy biểu đồ tăng trưởng thành công")
                .data(adService.getGrowthTrend())
                .build());
    }
}