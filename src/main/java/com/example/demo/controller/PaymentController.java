package com.example.demo.controller;

import com.example.demo.config.StripeConfig;
import com.example.demo.service.IPaymentService;
import com.example.demo.service.impl.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
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
    IPaymentService paymentService;
    StripeConfig stripeConfig;

    @PostMapping("/create-payment-intent/{orderId}")
    public ResponseEntity<?> createPaymentIntent(@PathVariable Long orderId) {
        try {
            com.stripe.Stripe.apiKey = stripeConfig.getApiKey();
            PaymentIntent pi = paymentService.createPaymentIntent(orderId);
            return ResponseEntity.ok(Map.of("clientSecret", pi.getClientSecret()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        System.out.println("Received webhook call");
        String endpointSecret = stripeConfig.getWebhookSecret();
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook signature verification failed");
        }

        System.out.println("Event type: " + event.getType());

        if ("payment_intent.succeeded".equals(event.getType())) {
            var optPaymentIntent = event.getDataObjectDeserializer().getObject();

            PaymentIntent paymentIntent = null;

            if (optPaymentIntent.isPresent()) {
                paymentIntent = (PaymentIntent) optPaymentIntent.get();
            } else {
                // fallback parse raw json manually
                String rawJson = event.getDataObjectDeserializer().getRawJson();
                paymentIntent = PaymentIntent.GSON.fromJson(rawJson, PaymentIntent.class);
            }

            if (paymentIntent != null) {
                System.out.println("Handling payment succeeded for id: " + paymentIntent.getId());
                paymentService.handlePaymentSucceeded(paymentIntent.getId());
            } else {
                System.out.println("Failed to parse PaymentIntent from webhook event");
            }
        }

        return ResponseEntity.ok("Received");
    }
}
