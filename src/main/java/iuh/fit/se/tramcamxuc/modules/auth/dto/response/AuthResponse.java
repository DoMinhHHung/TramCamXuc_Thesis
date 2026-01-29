package iuh.fit.se.tramcamxuc.modules.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthResponse {
    private String message;
    private String accessToken;
    private String refreshToken;
}
