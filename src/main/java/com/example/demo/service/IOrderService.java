package com.example.demo.service;

import com.example.demo.dto.request.CreateOrderRequest;
import com.example.demo.dto.request.UpdateOrderRequest;
import com.example.demo.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrderById(Long id);
    Page<OrderResponse> getOrders(Pageable pageable);
    OrderResponse updateOrder(Long orderId, UpdateOrderRequest request);
    OrderResponse approveOrder(Long orderId);
    OrderResponse prepareShipment(Long orderId);
    OrderResponse assignShipper(Long orderId);
    OrderResponse startShipping(Long orderId);
    OrderResponse confirmDelivery(Long orderId);
    OrderResponse cancelOrder(Long orderId);
    OrderResponse returnOrder(Long orderId);
}
