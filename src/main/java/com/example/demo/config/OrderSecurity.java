package com.example.demo.config;

import com.example.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {
    private final OrderRepository orderRepository;

    public boolean isOrderOwner(Long orderId, String username) {
        return orderRepository.findById(orderId)
                .map(order -> order.getCustomer().getUsername().equals(username))
                .orElse(false);
    }

    public boolean isAssignedShipper(Long orderId, String username) {
        return orderRepository.findById(orderId)
                .map(order -> order.getShipper() != null && order.getShipper().getUsername().equals(username))
                .orElse(false);
    }
}
