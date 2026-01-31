package iuh.fit.se.tramcamxuc.modules.advertisement.controller;

import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.request.UploadAdRequest;
import iuh.fit.se.tramcamxuc.modules.advertisement.service.AdvertisementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdvertisementController {

    private final AdvertisementService adService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadAd(@ModelAttribute @Valid UploadAdRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Upload quảng cáo thành công")
                .data(adService.uploadAdvertisement(request))
                .build());
    }

    @GetMapping("/random")
    public ResponseEntity<ApiResponse> getRandomAd() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy quảng cáo thành công")
                .data(adService.getRandomAdvertisement())
                .build());
    }
}