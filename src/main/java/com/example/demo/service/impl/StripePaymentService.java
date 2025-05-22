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
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service("stripePaymentService")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StripePaymentService implements IPaymentService {
    OrderRepository orderRepository;
    PaymentRepository paymentRepository;
    StripeConfig stripeConfig;

    @Override
    public PaymentIntent createPayment(Long orderId, String ipAddress) throws StripeException {
        Stripe.apiKey = stripeConfig.getApiKey();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrException.ORDER_NOT_FOUND));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!order.getCustomer().getUsername().equals(currentUsername)) {
            throw new AppException(ErrException.USER_ROLE_INVALID);
        }

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            throw new AppException(ErrException.ORDER_ALREADY_PAID);
        }

        Long amount = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("usd")
                .build();
        PaymentIntent pi = PaymentIntent.create(params);

        payment.setStripePaymentIntentId(pi.getId());
        paymentRepository.save(payment);

        return pi;
    }

    @Override
    public void handleCallback(Object callbackData) {
        String paymentIntentId = (String) callbackData;

        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new AppException(ErrException.PAYMENT_NOT_FOUND));

        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        orderRepository.save(order);
    }

    @Override
    public Payment.PaymentMethod getSupportedMethod() {
        return Payment.PaymentMethod.CREDIT_CARD;
    }
}
