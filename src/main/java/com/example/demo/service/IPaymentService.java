package com.example.demo.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

public interface IPaymentService {
    PaymentIntent createPaymentIntent(Long orderId) throws StripeException;
    void handlePaymentSucceeded(String paymentIntentId);
}
