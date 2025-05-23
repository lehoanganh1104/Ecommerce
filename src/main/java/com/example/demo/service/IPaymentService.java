package com.example.demo.service;

import jakarta.servlet.http.HttpServletRequest;

public interface IPaymentService {
    Object createPayment(Long orderId, String ipAddress);
    void handleCallback(HttpServletRequest request);
}
