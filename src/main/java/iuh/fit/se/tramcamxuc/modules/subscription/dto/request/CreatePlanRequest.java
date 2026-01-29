package iuh.fit.se.tramcamxuc.modules.subscription.dto.request;

import iuh.fit.se.tramcamxuc.modules.subscription.model.PlanFeatures;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePlanRequest {
    @NotBlank(message = "Tên gói không được để trống")
    private String name;

    private String description;

    @Min(value = 1000, message = "Giá tối thiểu 1000 VNĐ")
    private int price;

    @Min(value = 1, message = "Thời hạn ít nhất 1 ngày")
    private int durationDays;

    @NotNull(message = "Phải cấu hình tính năng cho gói")
    private PlanFeatures features;
}