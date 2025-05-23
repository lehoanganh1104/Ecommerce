package com.example.demo.service.impl;

import com.example.demo.config.VNPayConfig;
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

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("vnpayPaymentService")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayPaymentService implements IPaymentService {
    VNPayConfig vnpayConfig;
    PaymentRepository paymentRepository;
    OrderRepository orderRepository;

    @Override
    public Object createPayment(Long orderId, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrException.ORDER_NOT_FOUND));

        // Optional: Create a Payment record before redirecting to VNPay
        Payment payment = new Payment();
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
        Map<String, String[]> paramMap = request.getParameterMap();
        Map<String, String> vnpParams = new HashMap<>();

        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            if (entry.getValue().length > 0) {
                vnpParams.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        String responseCode = vnpParams.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            handleVnpSuccess(vnpParams);
        } else {
            handleVnpFailure(vnpParams);
        }
    }

    private void handleVnpSuccess(Map<String, String> vnpParams) {
        String transactionNo = vnpParams.get("vnp_TransactionNo");
        String orderIdStr = vnpParams.get("vnp_TxnRef");

        Long orderId = Long.valueOf(orderIdStr); // đảm bảo ID này được sinh theo định dạng Long
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrException.ORDER_NOT_FOUND));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrException.PAYMENT_NOT_FOUND));

        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        payment.setVnpTransactionNo(transactionNo); // ⬅️ thêm dòng này
        payment.setVnpResponseCode(vnpParams.get("vnp_ResponseCode"));
        payment.setVnpBankCode(vnpParams.get("vnp_BankCode"));
        payment.setVnpCardType(vnpParams.get("vnp_CardType"));
        paymentRepository.save(payment);

        order.setPaymentStatus(Order.PaymentStatus.PAID);
        orderRepository.save(order);

        log.info("✅ VNPay payment succeeded: transactionNo = {}", transactionNo);
    }

    private void handleVnpFailure(Map<String, String> vnpParams) {
        String transactionNo = vnpParams.get("vnp_TransactionNo");
        String orderIdStr = vnpParams.get("vnp_TxnRef");

        Long orderId = Long.valueOf(orderIdStr);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrException.PAYMENT_NOT_FOUND));

        payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
        payment.setVnpTransactionNo(transactionNo); // thêm
        payment.setVnpResponseCode(vnpParams.get("vnp_ResponseCode"));
        paymentRepository.save(payment);

        log.warn("❌ VNPay payment failed: transactionNo = {}, responseCode = {}", transactionNo, vnpParams.get("vnp_ResponseCode"));

    }
}
