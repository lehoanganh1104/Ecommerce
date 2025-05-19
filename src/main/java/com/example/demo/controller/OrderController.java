package com.example.demo.controller;

import com.example.demo.dto.request.CreateOrderRequest;
import com.example.demo.dto.request.UpdateOrderRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.IOrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        try {
            OrderResponse response = orderService.createOrder(request);
            ApiResponse<OrderResponse> apiResponse = ApiResponse.success(response);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex){
            ApiResponse<OrderResponse> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @GetMapping
    public ResponseEntity<?> getOrders( @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderResponse> responses = orderService.getOrders(pageable);
            ApiResponse<Page<OrderResponse>> apiResponse = ApiResponse.success(responses);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex) {
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            OrderResponse response = orderService.getOrderById(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AppException ex) {
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @PutMapping("/update/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderRequest request) {
        try {
            OrderResponse response = orderService.updateOrder(orderId, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AppException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage()));
        }
    }

    @PatchMapping("/update/{orderId}/approve")
    public ResponseEntity<ApiResponse<OrderResponse>> approveOrder(@PathVariable Long orderId) {
        try {
            OrderResponse response = orderService.approveOrder(orderId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AppException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage()));
        }
    }

    @PatchMapping("/update/{orderId}/prepare-shipment")
    public ResponseEntity<ApiResponse<OrderResponse>> prepareShipment(@PathVariable Long orderId) {
        try {
            OrderResponse response = orderService.prepareShipment(orderId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AppException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage()));
        }
    }

    @PatchMapping("/update/{orderId}/assign-shipper")
    public ResponseEntity<ApiResponse<OrderResponse>> assignShipper(
            @PathVariable Long orderId) {
        try {
            OrderResponse response = orderService.assignShipper(orderId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AppException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage()));
        }
    }

    @PatchMapping("/update/{orderId}/start-shipping")
    public ResponseEntity<ApiResponse<OrderResponse>> startShipping(@PathVariable Long orderId) {
        try {
            OrderResponse response = orderService.startShipping(orderId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AppException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage()));
        }
    }

    @PatchMapping("/update/{orderId}/confirm-delivery")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmDelivery(@PathVariable Long orderId) {
        try {
            OrderResponse response = orderService.confirmDelivery(orderId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AppException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage()));
        }
    }

    @PatchMapping("/update/{orderId}/return")
    public ResponseEntity<ApiResponse<OrderResponse>> returnOrder(@PathVariable Long orderId) {
        try {
            OrderResponse response = orderService.returnOrder(orderId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AppException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            ex.getErrException().getCode(),
                            ex.getErrException().getMessage()
                    ));
        }
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long orderId) {
        try {
            OrderResponse response = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AppException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage()));
        }
    }
}
