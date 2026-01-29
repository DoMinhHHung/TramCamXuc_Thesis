package iuh.fit.se.tramcamxuc.modules.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import iuh.fit.se.tramcamxuc.common.exception.dto.ApiResponse;
import iuh.fit.se.tramcamxuc.modules.payment.dto.request.CreatePaymentRequest;
import iuh.fit.se.tramcamxuc.modules.payment.dto.response.PaymentResponse;
import iuh.fit.se.tramcamxuc.modules.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final PayOS payOS;
    private final ObjectMapper objectMapper;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPaymentLink(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.createPaymentLink(request)));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<String>> handleWebhook(@RequestBody String body) {
        try {
            ObjectNode node = (ObjectNode) objectMapper.readTree(body);
            WebhookData webhookData = payOS.webhooks().verify(node);
            paymentService.processWebhook(webhookData);
            return ResponseEntity.ok(ApiResponse.success("Webhook processed"));
        } catch (Exception e) {
            log.error("Webhook Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid Webhook"));
        }
    }
}