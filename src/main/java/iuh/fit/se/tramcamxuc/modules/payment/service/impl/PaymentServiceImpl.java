package iuh.fit.se.tramcamxuc.modules.payment.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.modules.payment.dto.request.CreatePaymentRequest;
import iuh.fit.se.tramcamxuc.modules.payment.dto.response.PaymentResponse;
import iuh.fit.se.tramcamxuc.modules.payment.entity.PaymentTransaction;
import iuh.fit.se.tramcamxuc.modules.payment.entity.enums.PaymentStatus;
import iuh.fit.se.tramcamxuc.modules.payment.repository.PaymentTransactionRepository;
import iuh.fit.se.tramcamxuc.modules.payment.service.PaymentService;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.SubscriptionPlan;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.UserSubscription;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.enums.SubscriptionStatus;
import iuh.fit.se.tramcamxuc.modules.subscription.repository.SubscriptionPlanRepository;
import iuh.fit.se.tramcamxuc.modules.subscription.repository.UserSubscriptionRepository;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PayOS payOS;
    private final PaymentTransactionRepository transactionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserService userService;

    @Value("${payos.return-url}")
    private String defaultReturnUrl;

    @Value("${payos.cancel-url}")
    private String defaultCancelUrl;

    @Override
    @Transactional
    public PaymentResponse createPaymentLink(CreatePaymentRequest request) {
        User user = userService.getCurrentUser();
        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plans not exists"));

        long orderCode = generateOrderCode();

        PaymentLinkItem item = PaymentLinkItem .builder()
                .name(plan.getName())
                .price((long) plan.getPrice())
                .quantity(1)
                .build();

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest .builder()
                .orderCode(orderCode)
                .amount((long) plan.getPrice())
                .description("Buy " + plan.getName())
                .returnUrl(request.getReturnUrl() != null ? request.getReturnUrl() : defaultReturnUrl)
                .cancelUrl(request.getCancelUrl() != null ? request.getCancelUrl() : defaultCancelUrl)
                .item(item)
                .build();

        try {
            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

            PaymentTransaction transaction = PaymentTransaction.builder()
                    .orderCode(orderCode)
                    .user(user)
                    .plan(plan)
                    .amount(plan.getPrice())
                    .status(PaymentStatus.PENDING)
                    .checkoutUrl(data.getCheckoutUrl())
                    .build();
            transactionRepository.save(transaction);

            return PaymentResponse.builder()
                    .checkoutUrl(data.getCheckoutUrl())
                    .orderCode(orderCode)
                    .qrCode(data.getQrCode())
                    .build();

        } catch (Exception e) {
            log.error("PayOS Error: ", e);
            throw new AppException("Cannot create payment link: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processWebhook(WebhookData webhookData) {
        Long orderCode = webhookData.getOrderCode();
        log.info("Webhook received orderCode: {}", orderCode);

        PaymentTransaction transaction = transactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found " + orderCode));

        if (PaymentStatus.PAID.equals(transaction.getStatus())) return;

        if ("00".equals(webhookData.getCode())) {
            transaction.setStatus(PaymentStatus.PAID);
            transactionRepository.save(transaction);

            activateSubscription(transaction.getUser(), transaction.getPlan());

        } else {
            transaction.setStatus(PaymentStatus.CANCELLED);
            transactionRepository.save(transaction);
        }
    }

    private void activateSubscription(User user, SubscriptionPlan plan) {
        UserSubscription subscription = userSubscriptionRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
                .orElse(UserSubscription.builder()
                        .user(user)
                        .status(SubscriptionStatus.ACTIVE)
                        .build());

        subscription.setPlan(plan);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusDays(plan.getDurationDays()));
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        userSubscriptionRepository.save(subscription);
    }

    private long generateOrderCode() {
        long timestamp = System.currentTimeMillis();
        int randomThreeDigits = new SecureRandom().nextInt(900) + 100;
        return Long.parseLong(String.valueOf(timestamp) + randomThreeDigits);
    }
}