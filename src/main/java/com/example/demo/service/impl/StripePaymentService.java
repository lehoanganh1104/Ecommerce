package com.example.demo.service.impl;

import com.example.demo.config.StripeConfig;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.model.Order;
import com.example.demo.model.Payment;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.IPaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service("stripePaymentService")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StripePaymentService implements IPaymentService {
    StripeConfig stripeConfig;
    PaymentRepository paymentRepository;
    OrderRepository orderRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeConfig.getApiKey();
    }

    @Override
    public Object createPayment(Long orderId, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrException.ORDER_NOT_FOUND));

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            throw new AppException(ErrException.ORDER_ALREADY_PAID);
        }

        try {
            // Stripe uses cents, so multiply by 100
            long amount = order.getTotalAmount().multiply(new BigDecimal(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("vnd")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(order.getTotalAmount());
            payment.setPaymentMethod(Payment.PaymentMethod.STRIPE);
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStripePaymentIntentId(paymentIntent.getId());

            paymentRepository.save(payment);

            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());

            return response;
        } catch (StripeException e) {
            log.error("Stripe error: ", e);
            throw new AppException(ErrException.STRIPE_INTEGRATION_ERROR);
        }
    }

    @Override
    public void handleCallback(HttpServletRequest request) {
        String payload;
        String sigHeader = request.getHeader("Stripe-Signature");

        try {
            payload = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AppException(ErrException.ERR_EXCEPTION);
        }
        log.info("Stripe webhook raw payload: {}", payload);

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (Exception e) {
            log.error("⚠️ Webhook error: {}", e.getMessage());
            throw new AppException(ErrException.STRIPE_INTEGRATION_ERROR);
        }
        log.info("Received Stripe event: {}", event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded": {
                var deserializer = event.getDataObjectDeserializer();
                if (deserializer.getObject().isPresent() && deserializer.getObject().get() instanceof PaymentIntent paymentIntent) {
                    handlePaymentIntentSucceeded(paymentIntent);
                } else {
                    log.warn("Could not deserialize event data object for event type: payment_intent.succeeded");
                }
                break;
            }
            case "charge.succeeded": {
                var deserializer = event.getDataObjectDeserializer();
                if (deserializer.getObject().isPresent() && deserializer.getObject().get() instanceof com.stripe.model.Charge charge) {
                    handleChargeSucceeded(charge);
                } else {
                    log.warn("Could not deserialize event data object for event type: charge.succeeded");
                }
                break;
            }
            default:
                log.info("Ignoring event type: {}", event.getType());
                break;
        }
    }

    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        String paymentIntentId = paymentIntent.getId();
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new AppException(ErrException.PAYMENT_NOT_FOUND));

        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        orderRepository.save(order);

        log.info("✅ Payment succeeded for paymentIntentId: {}", paymentIntentId);
    }

    private void handleChargeSucceeded(com.stripe.model.Charge charge) {
        String paymentIntentId = charge.getPaymentIntent();
        if (paymentIntentId == null) {
            log.warn("Charge event without paymentIntent ID, ignoring.");
            return;
        }
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new AppException(ErrException.PAYMENT_NOT_FOUND));

        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        orderRepository.save(order);

        log.info("✅ Payment succeeded for charge linked to paymentIntentId: {}", paymentIntentId);
    }
}
