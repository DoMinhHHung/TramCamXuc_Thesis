package iuh.fit.se.tramcamxuc.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyAccountRequest {
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "OTP is required")
    private String otp;
}
