package com.example.demo.dto.response;

import com.example.demo.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Payment.PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;
    private Payment.PaymentStatus paymentStatus;
    private String stripePaymentIntentId;
}
