package iuh.fit.se.tramcamxuc.modules.payment.service;

import iuh.fit.se.tramcamxuc.modules.payment.dto.request.CreatePaymentRequest;
import iuh.fit.se.tramcamxuc.modules.payment.dto.response.PaymentResponse;
import vn.payos.model.webhooks.WebhookData;

public interface PaymentService {
    PaymentResponse createPaymentLink(CreatePaymentRequest request);
    void processWebhook(WebhookData webhookData);
}