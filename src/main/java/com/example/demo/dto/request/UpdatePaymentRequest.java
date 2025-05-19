package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentRequest {
    @NotNull(message = "ORDER_ID_NULL")
    private Long orderId;

    @NotBlank(message = "PAYMENT_INTENT_ID_NULL")
    private String paymentIntentId;
}
