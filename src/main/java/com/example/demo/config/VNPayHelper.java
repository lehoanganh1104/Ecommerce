package com.example.demo.config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class VNPayHelper {
    public static String hashAllFields(Map<String, String> fields, String secretKey) {
        Map<String, String> sortedParams = new TreeMap<>(fields);
        sortedParams.remove("vnp_SecureHash");
        sortedParams.remove("vnp_SecureHashType");

        String data = sortedParams.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        return hmacSHA512(secretKey, data);
    }

    public static String hmacSHA512(String key, String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(secretKey);
            byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hmacData) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi khi tạo chữ ký HMAC SHA512", ex);
        }
    }

    public static String buildHashData(Map<String, String> fields) {
        // Dùng TreeMap để đảm bảo sắp xếp theo thứ tự key chữ cái
        Map<String, String> sortedParams = new TreeMap<>(fields);

        return sortedParams.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }
}
