package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vnpay")
public class VNPayProperties {
    private String secretKey;
    private String tmnCode;
    private String returnUrl;
    private String payUrl;
}
