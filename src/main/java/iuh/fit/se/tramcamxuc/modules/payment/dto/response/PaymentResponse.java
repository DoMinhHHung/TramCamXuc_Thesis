package iuh.fit.se.tramcamxuc.modules.payment.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String checkoutUrl;
    private Long orderCode;
    private String qrCode;
}