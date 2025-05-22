package com.example.demo.service.impl;

import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.model.Payment;
import com.example.demo.service.IPaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public
class PaymentService {
    List<IPaymentService> paymentServices;

    private IPaymentService resolveService(Payment.PaymentMethod method) {
        return paymentServices.stream()
                .filter(service -> service.getSupportedMethod() == method)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrException.UNSUPPORTED_PAYMENT_METHOD));
    }

    public Object createPayment(Long orderId, String ipAddress, Payment.PaymentMethod method) throws Exception {
        return resolveService(method).createPayment(orderId, ipAddress);
    }

    public void handleCallback(Payment.PaymentMethod method, Object callbackData) {
        resolveService(method).handleCallback(callbackData);
    }
}

