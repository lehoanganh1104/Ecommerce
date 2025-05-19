package com.example.demo.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateOrderRequest {
    private String shippingAddress;
    private List<OrderDetailRequest> items;

    @Data
    public static class OrderDetailRequest {
        private Long productId;
        private Integer quantity;
    }
}
