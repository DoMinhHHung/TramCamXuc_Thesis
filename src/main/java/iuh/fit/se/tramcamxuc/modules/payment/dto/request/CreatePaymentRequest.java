package iuh.fit.se.tramcamxuc.modules.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreatePaymentRequest {
    @NotNull(message = "Vui lòng chọn gói cước")
    private UUID planId;

    private String returnUrl;
    private String cancelUrl;
}