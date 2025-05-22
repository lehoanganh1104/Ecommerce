package com.example.demo.service;

import com.example.demo.model.Payment;

public interface IPaymentService {
    Object createPayment(Long orderId, String ipAddress) throws Exception;
    void handleCallback(Object callbackData);
    Payment.PaymentMethod getSupportedMethod();
}
