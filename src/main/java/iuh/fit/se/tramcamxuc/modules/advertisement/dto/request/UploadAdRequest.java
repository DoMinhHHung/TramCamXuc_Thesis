package iuh.fit.se.tramcamxuc.modules.advertisement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadAdRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Tên nhà tài trợ không được để trống")
    private String sponsorName;

    private String clickUrl;

    @NotNull(message = "File quảng cáo không được để trống")
    private MultipartFile file;
}