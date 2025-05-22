package com.example.demo.controller;

import com.example.demo.common.constants.SuccessMessage;
import com.example.demo.config.StripeConfig;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.model.Payment;
import com.example.demo.service.IPaymentService;
import com.example.demo.service.impl.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Object>> createPayment(
            @RequestParam Long orderId,
            @RequestParam Payment.PaymentMethod method,
            HttpServletRequest request
    ) throws Exception {
        String ipAddress = request.getRemoteAddr();
        Object result = paymentService.createPayment(orderId, ipAddress, method);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Object>builder()
                        .status(HttpStatus.CREATED.value())
                        .message(SuccessMessage.PAYMENT_CREATED)
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<String>> handleVnPayReturn(@RequestParam Map<String, String> params) {
        paymentService.handleCallback(Payment.PaymentMethod.VNPAY, params);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(HttpStatus.OK.value())
                        .message(SuccessMessage.PAYMENT_SUCCESS)
                        .data("VNPAY callback processed")
                        .build()
        );
    }

    @PostMapping("/stripe-webhook")
    public ResponseEntity<ApiResponse<String>> handleStripeWebhook(@RequestBody String payload) {
        paymentService.handleCallback(Payment.PaymentMethod.CREDIT_CARD, payload);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(HttpStatus.OK.value())
                        .message(SuccessMessage.PAYMENT_SUCCESS)
                        .data("Stripe webhook processed")
                        .build()
        );
    }
}
