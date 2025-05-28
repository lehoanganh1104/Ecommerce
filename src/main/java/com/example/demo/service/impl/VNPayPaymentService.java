package com.example.demo.service.impl;

import com.example.demo.config.VNPayConfig;
import com.example.demo.config.VNPayHelper;
import com.example.demo.config.VNPayProperties;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.model.Order;
import com.example.demo.model.Payment;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.IPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("vnpayPaymentService")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayPaymentService implements IPaymentService {
    VNPayConfig vnpayConfig;
    PaymentRepository paymentRepository;
    OrderRepository orderRepository;
    VNPayProperties vnPayProperties;

    @Override
    public Object createPayment(Long orderId, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrException.ORDER_NOT_FOUND));

        String vnpTxnRef = String.valueOf(System.currentTimeMillis());

        // Optional: Create a Payment record before redirecting to VNPay
        Payment payment = new Payment();
        payment.setVnpTxnRef(vnpTxnRef);
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(Payment.PaymentMethod.VNPAY);
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return vnpayConfig.buildPaymentUrl(order, ipAddress);
    }

    @Override
    @Transactional
    public void handleCallback(HttpServletRequest request) {
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .filter(e -> e.getValue().length > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));

        // 🔐 Bước 1: Xác thực chữ ký
        String receivedHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        log.info("📥 VNPay callback parameters:");
        params.forEach((k, v) -> log.info("{} = {}", k, v));

        // In ra chuỗi hash data
        String hashData = VNPayHelper.buildHashData(params); // Cần đảm bảo bạn có hàm này riêng
        String computedHash = VNPayHelper.hmacSHA512(vnPayProperties.getSecretKey().trim(), hashData);

        log.info("🔐 Hash data: {}", hashData);
        log.info("🔑 Received Hash: {}", receivedHash);
        log.info("🔑 Computed Hash: {}", computedHash);

        if (!computedHash.equalsIgnoreCase(receivedHash)) {
            log.warn("❌ Invalid VNPay signature");
            throw new AppException(ErrException.INVALID_SIGNATURE);
        }

        // 🔁 Bước 2: Xử lý kết quả thanh toán
        if ("00".equals(params.get("vnp_ResponseCode"))) {
            handleSuccess(params);
        } else {
            handleFailure(params);
        }
    }

    private void handleSuccess(Map<String, String> params) {
        Long orderId = Long.valueOf(params.get("vnp_TxnRef"));
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrException.PAYMENT_NOT_FOUND));

        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        payment.setVnpTransactionNo(params.get("vnp_TransactionNo"));
        payment.setVnpResponseCode(params.get("vnp_ResponseCode"));
        payment.setVnpBankCode(params.get("vnp_BankCode"));
        payment.setVnpCardType(params.get("vnp_CardType"));
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        orderRepository.save(order);

        log.info("✅ VNPay payment success: {}", params.get("vnp_TransactionNo"));
    }

    private void handleFailure(Map<String, String> params) {
        Long orderId = Long.valueOf(params.get("vnp_TxnRef"));
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrException.PAYMENT_NOT_FOUND));

        payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
        payment.setVnpTransactionNo(params.get("vnp_TransactionNo"));
        payment.setVnpResponseCode(params.get("vnp_ResponseCode"));
        paymentRepository.save(payment);

        log.warn("❌ VNPay payment failed: {}", params.get("vnp_ResponseCode"));
    }
}
