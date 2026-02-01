package iuh.fit.se.tramcamxuc.modules.advertisement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAdRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Tên nhà tài trợ không được để trống")
    private String sponsorName;

    private String clickUrl;
}
