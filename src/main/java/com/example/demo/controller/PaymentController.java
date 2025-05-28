package com.example.demo.controller;

import com.example.demo.model.Payment;
import com.example.demo.service.impl.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;

    @PostMapping("/create-payment/{orderId}")
    public Object createPayment(@PathVariable Long orderId,
                                @RequestParam String method,
                                HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        return paymentService.createPayment(orderId, ipAddress, Payment.PaymentMethod.valueOf(method.toUpperCase()));
    }

    @PostMapping("/callback/{method}")
    public void handleCallback(@PathVariable String method,
                               HttpServletRequest request) {
        Payment.PaymentMethod paymentMethod;
        try {
            paymentMethod = Payment.PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment method");
        }
        paymentService.handleCallback(paymentMethod.name(), request);
    }
}
