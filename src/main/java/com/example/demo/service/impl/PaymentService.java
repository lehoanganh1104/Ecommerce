package com.example.demo.service.impl;

import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.model.Payment;
import com.example.demo.service.IPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final Map<String, IPaymentService> paymentServiceMap;

    public Object createPayment(Long orderId, String ipAddress, Payment.PaymentMethod method) {
        IPaymentService service = resolveService(method);
        return service.createPayment(orderId, ipAddress);
    }

    public void handleCallback(String method, HttpServletRequest request) {
        IPaymentService service = resolveService(Payment.PaymentMethod.valueOf(method.toUpperCase()));
        service.handleCallback(request);
    }

    private IPaymentService resolveService(Payment.PaymentMethod method) {
        String beanName = switch (method) {
            case STRIPE -> "stripePaymentService";
            case VNPAY -> "vnpayPaymentService";
            default -> throw new AppException(ErrException.PAYMENT_METHOD_NOT_SUPPORTED);
        };
        IPaymentService service = paymentServiceMap.get(beanName);
        if (service == null) throw new AppException(ErrException.PAYMENT_METHOD_NOT_SUPPORTED);
        return service;
    }
}
