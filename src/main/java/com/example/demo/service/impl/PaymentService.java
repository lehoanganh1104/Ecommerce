package com.example.demo.service;

import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.model.Order;
import com.example.demo.model.Payment;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService implements IPaymentService{
    OrderRepository orderRepository;
    PaymentRepository paymentRepository;

    @Override
    public PaymentIntent createPaymentIntent(Long orderId) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrException.ORDER_NOT_EXISTED));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!order.getCustomer().getUserName().equals(currentUsername)) {
            throw new AppException(ErrException.USER_INVALID_ROLE);
        }

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            throw new AppException(ErrException.ORDER_ALREADY_PAID);
        }

        String currency = "usd";
        // USD => CENT (x100)
        Long amount = order.getTotalAmount()
                .multiply(new BigDecimal(100))
                .longValue();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .build();
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        payment.setStripePaymentIntentId(paymentIntent.getId());
        paymentRepository.save(payment);

        return paymentIntent;
    }

    public void handlePaymentSucceeded(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new AppException(ErrException.PAYMENT_NOT_EXISTED));

        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        orderRepository.save(order);
    }
}

