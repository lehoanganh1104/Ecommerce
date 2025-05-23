package com.example.demo.config;

import com.example.demo.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
public class VNPayConfig {
    private final VNPayProperties vnPayProperties;

    public String buildPaymentUrl(Order order, String ipAddress) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnpCreateDate = LocalDateTime.now().format(formatter);
        String vnpExpireDate = LocalDateTime.now().plusMinutes(15).format(formatter); // thêm thời gian hết hạn
        String vnpTxnRef = String.valueOf(System.currentTimeMillis());

        long amount = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue(); // VND × 100

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId());
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        vnpParams.put("vnp_OrderType", "other");

        // Sắp xếp tham số
        SortedMap<String, String> sortedParams = new TreeMap<>(vnpParams);

        // Tạo chuỗi để ký – loại bỏ vnp_SecureHash
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            hashData.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
        }
        hashData.deleteCharAt(hashData.length() - 1);

        // Tạo chữ ký
        String secureHash = VNPayHelper.hmacSHA512(vnPayProperties.getSecretKey().trim(), hashData.toString());

        // Gắn chữ ký vào query URL (có encode)
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)).append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append('&');
        }

        return vnPayProperties.getPayUrl() + "?" + query.toString();
    }
}
