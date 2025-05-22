package com.example.demo.service.impl;

import com.example.demo.common.constants.Utils.VnPayProperties;
import com.example.demo.common.constants.Utils.VnPayUtils;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.model.Order;
import com.example.demo.model.Payment;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.IPaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service("vnpayPaymentService")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VnPayPaymentService implements IPaymentService {
    OrderRepository orderRepository;
    PaymentRepository paymentRepository;
    VnPayProperties vnpayProperties;

    @Override
    public Object createPayment(Long orderId, String ipAddress) throws Exception {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrException.ORDER_NOT_FOUND));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!order.getCustomer().getUsername().equals(currentUsername)) {
            throw new AppException(ErrException.USER_ROLE_INVALID);
        }

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            throw new AppException(ErrException.ORDER_ALREADY_PAID);
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(Payment.PaymentMethod.VNPAY);
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        BigDecimal amount = order.getTotalAmount();
        return VnPayUtils.generatePaymentUrl(orderId, amount, ipAddress);
    }

    @Override
    public void handleCallback(Object callbackData) {
        String secretKey = vnpayProperties.getSecretKey();
        String tmnCode = vnpayProperties.getTmnCode();
        Map<String, String> params = (Map<String, String>) callbackData;

        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null) {
            throw new AppException(ErrException.VNPAY_MISSING_SECURE_HASH);
        }

        Map<String, String> filteredParams = params.entrySet().stream()
                .filter(e -> !e.getKey().equals("vnp_SecureHash") && !e.getKey().equals("vnp_SecureHashType"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String calculatedHash = VnPayUtils.hashAllFields(filteredParams, secretKey);

        if (!vnpSecureHash.equalsIgnoreCase(calculatedHash)) {
            throw new AppException(ErrException.VNPAY_CHECKSUM_INVALID);
        }
        String vnpTmnCode = params.get("vnp_TmnCode");
        if (!tmnCode.equals(vnpTmnCode)) {
            throw new AppException(ErrException.VNPAY_INVALID_MERCHANT_CODE);
        }

        String vnpTxnNo = params.get("vnp_TransactionNo");
        String responseCode = params.get("vnp_ResponseCode");
        String vnpAmountStr = params.get("vnp_Amount");

        Payment payment = paymentRepository.findByVnpTransactionNo(vnpTxnNo)
                .orElseThrow(() -> new AppException(ErrException.PAYMENT_NOT_FOUND));

        BigDecimal vnpAmount = new BigDecimal(vnpAmountStr).divide(BigDecimal.valueOf(100));
        BigDecimal orderAmount = payment.getOrder().getTotalAmount();

        if (vnpAmount.compareTo(orderAmount) != 0) {
            throw new AppException(ErrException.VNPAY_AMOUNT_MISMATCH);
        }

        if ("00".equals(responseCode)) {
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        } else {
            payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
        }

        payment.setPaymentDate(LocalDateTime.now());
        payment.setVnpTransactionNo(vnpTxnNo);
        payment.setVnpResponseCode(responseCode);
        payment.setVnpBankCode(params.get("vnp_BankCode"));
        payment.setVnpCardType(params.get("vnp_CardType"));
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setPaymentStatus("00".equals(responseCode) ? Order.PaymentStatus.PAID : Order.PaymentStatus.UNPAID);
        orderRepository.save(order);
    }

    @Override
    public Payment.PaymentMethod getSupportedMethod() {
        return Payment.PaymentMethod.VNPAY;
    }
}
