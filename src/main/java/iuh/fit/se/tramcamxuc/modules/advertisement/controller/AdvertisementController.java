package iuh.fit.se.tramcamxuc.modules.advertisement.controller;

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
@RequestMapping("/api/v1/ads")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService adService;

    // --- ADMIN ENDPOINTS ---
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> uploadAd(@ModelAttribute @Valid UploadAdRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Upload quảng cáo thành công")
                .data(adService.uploadAdvertisement(request))
                .build());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAdById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy thông tin quảng cáo thành công")
                .data(adService.getAdById(id))
                .build());
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteAd(@PathVariable UUID id) {
        adService.deleteAd(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Xóa quảng cáo thành công")
                .build());
    }

    @PutMapping("/admin/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> toggleAdStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Cập nhật trạng thái quảng cáo thành công")
                .data(adService.toggleAdStatus(id))
                .build());
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getStatistics() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy thống kê quảng cáo thành công")
                .data(adService.getStatistics())
                .build());
    }

    @GetMapping("/admin/growth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getGrowthTrend() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy biểu đồ tăng trưởng thành công")
                .data(adService.getGrowthTrend())
                .build());
    }

    // --- PUBLIC ENDPOINTS ---
    
    @GetMapping("/random")
    public ResponseEntity<ApiResponse> getRandomAd() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy quảng cáo thành công")
                .data(adService.getRandomAdvertisement())
                .build());
    }

    @PostMapping("/{id}/impression")
    public ResponseEntity<ApiResponse> recordImpression(@PathVariable UUID id) {
        adService.recordImpression(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Đã ghi nhận lượt hiển thị")
                .build());
    }

    @PostMapping("/{id}/click")
    public ResponseEntity<ApiResponse> recordClick(@PathVariable UUID id) {
        adService.recordClick(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Đã ghi nhận lượt click")
                .build());
    }
}