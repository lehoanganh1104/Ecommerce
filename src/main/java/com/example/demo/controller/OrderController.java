package com.example.demo.controller;

import com.example.demo.common.constants.SuccessMessage;
import com.example.demo.dto.request.CreateOrderRequest;
import com.example.demo.dto.request.UpdateOrderRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.service.IOrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    IOrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<OrderResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message(SuccessMessage.ORDER_CREATED)
                        .data(response)
                        .build()
        );

    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders( @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> responses = orderService.getOrders(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<OrderResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.ORDERS_FETCHED)
                .data(responses)
                .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.ORDER_FETCHED)
                .data(response)
                .build()
        );
    }

    @PutMapping("/update/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderRequest request) {
        OrderResponse response = orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.ORDER_UPDATED)
                .data(response)
                .build()
        );
    }

    @PatchMapping("/update/{orderId}/approve")
    public ResponseEntity<ApiResponse<OrderResponse>> approveOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.approveOrder(orderId);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.ORDER_UPDATED)
                .data(response)
                .build()
        );
    }

    @PatchMapping("/update/{orderId}/prepare-shipment")
    public ResponseEntity<ApiResponse<OrderResponse>> prepareShipment(@PathVariable Long orderId) {
        OrderResponse response = orderService.prepareShipment(orderId);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.ORDER_UPDATED)
                .data(response)
                .build()
        );
    }

    @PatchMapping("/update/{orderId}/assign-shipper")
    public ResponseEntity<ApiResponse<OrderResponse>> assignShipper(
            @PathVariable Long orderId) {
        OrderResponse response = orderService.assignShipper(orderId);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.ORDER_UPDATED)
                .data(response)
                .build()
        );
    }

    @PatchMapping("/update/{orderId}/start-shipping")
    public ResponseEntity<ApiResponse<OrderResponse>> startShipping(@PathVariable Long orderId) {
        OrderResponse response = orderService.startShipping(orderId);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.ORDER_UPDATED)
                .data(response)
                .build()
        );
    }

    @PatchMapping("/update/{orderId}/confirm-delivery")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmDelivery(@PathVariable Long orderId) {
        OrderResponse response = orderService.confirmDelivery(orderId);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.ORDER_UPDATED)
                .data(response)
                .build()
        );
    }

    @PatchMapping("/update/{orderId}/return")
    public ResponseEntity<ApiResponse<OrderResponse>> returnOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.returnOrder(orderId);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.ORDER_RETURNED)
                .data(response)
                .build()
        );
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.ORDER_CANCELED)
                .data(response)
                .build()
        );
    }
}
