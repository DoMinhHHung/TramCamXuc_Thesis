package iuh.fit.se.tramcamxuc.modules.user.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String oldPassword;

    @NotBlank(message = "OTP không được để trống")
    private String otp;

    @Size(min = 6, message = "Password mới phải từ 6 ký tự")
    private String newPassword;

    private String confirmPassword;
}