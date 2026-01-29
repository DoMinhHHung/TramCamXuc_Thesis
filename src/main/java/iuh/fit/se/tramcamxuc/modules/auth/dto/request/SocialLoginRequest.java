package iuh.fit.se.tramcamxuc.modules.auth.dto.request;

import iuh.fit.se.tramcamxuc.modules.user.entity.enums.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginRequest {
    @NotBlank(message = "Token không được để trống")
    private String token;

    @NotNull(message = "Provider không được để trống")
    private AuthProvider provider;
}