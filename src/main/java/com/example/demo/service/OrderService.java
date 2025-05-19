package com.example.demo.service;

import com.example.demo.dto.request.CreateOrderRequest;
import com.example.demo.dto.request.UpdateOrderRequest;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.mapper.IOrderMapper;
import com.example.demo.model.Order;
import com.example.demo.model.OrderDetail;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService implements IOrderService{
    OrderRepository orderRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    IOrderMapper orderMapper;

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public OrderResponse createOrder(CreateOrderRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User customer = userRepository.findByUserNameAndDeletedFalse(username)
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_EXISTED));

        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(request.getShippingAddress());
        order.setOrderStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.UNPAID);

        Set<OrderDetail> details = new HashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderDetailRequest item : request.getItems()) {
            Product product = productRepository.findByIdAndDeletedFalse(item.getProductId())
                    .orElseThrow(() -> new AppException(ErrException.PRODUCT_NOT_EXISTED));

            if (item.getQuantity() <= 0) {
                throw new AppException(ErrException.INVALID_QUANTITY);
            }

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new AppException(ErrException.PRODUCT_OUT_OF_STOCK);
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());

            BigDecimal price = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            detail.setPrice(price);

            details.add(detail);
            totalAmount = totalAmount.add(price);
        }

        order.setOrderDetails(details);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER') or @orderSecurity.isOrderOwner(#id, authentication.name)")
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrException.ORDER_NOT_EXISTED));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER', 'CUSTOMER')")
    public Page<OrderResponse> getOrders(Pageable pageable) {
        User currentUser = getCurrentUser();

        if (User.Role.CUSTOMER.equals(currentUser.getRole())) {
            Page<Order> orders = orderRepository.findByCustomerId(currentUser.getId(), pageable);
            return orders.map(orderMapper::toOrderResponse);
        }

        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER') or @orderSecurity.isOrderOwner(#orderId, authentication.name)")
    public OrderResponse updateOrder(Long orderId, UpdateOrderRequest request) {
        Order order = getOrderOrThrow(orderId);

        order.getOrderDetails().clear();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (UpdateOrderRequest.OrderDetailRequest item : request.getItems()) {
            Product product = productRepository.findByIdAndDeletedFalse(item.getProductId())
                    .orElseThrow(() -> new AppException(ErrException.PRODUCT_NOT_EXISTED));

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new AppException(ErrException.INVALID_QUANTITY);
            }

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new AppException(ErrException.PRODUCT_OUT_OF_STOCK);
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());

            BigDecimal price = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            detail.setPrice(price);

            order.getOrderDetails().add(detail);
            totalAmount = totalAmount.add(price);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public OrderResponse approveOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        validateOrderStatus(order, Order.OrderStatus.PENDING);

        order.setOrderStatus(Order.OrderStatus.PROCESSING);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public OrderResponse prepareShipment(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        validateOrderStatus(order, Order.OrderStatus.PROCESSING);

        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();
            if (product.getStockQuantity() < detail.getQuantity()) {
                throw new AppException(ErrException.PRODUCT_OUT_OF_STOCK);
            }
            product.setStockQuantity(product.getStockQuantity() - detail.getQuantity());
            productRepository.save(product);
        }

        order.setOrderStatus(Order.OrderStatus.READY_TO_SHIP);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SHIPPER')")
    public OrderResponse assignShipper(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        validateOrderStatus(order, Order.OrderStatus.READY_TO_SHIP);

        User shipper = getCurrentUser();

        if (shipper.getRole() != User.Role.SHIPPER) {
            throw new AppException(ErrException.USER_NOT_SHIPPER);
        }

        if (order.getShipper() != null && !order.getShipper().getId().equals(shipper.getId())) {
            throw new AppException(ErrException.ORDER_ALREADY_HAVE_SHIPPER);
        }

        if (order.getShipper() == null) {
            order.setShipper(shipper);
        }

        order.setShipper(shipper);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SELLER') or (hasRole('SHIPPER') and @orderSecurity.isAssignedShipper(#orderId, authentication.name))")
    public OrderResponse startShipping(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        validateOrderStatus(order, Order.OrderStatus.READY_TO_SHIP);

        order.setOrderStatus(Order.OrderStatus.SHIPPED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SELLER') or (hasRole('SHIPPER') and @orderSecurity.isAssignedShipper(#orderId, authentication.name))")
    public OrderResponse confirmDelivery(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        validateOrderStatus(order, Order.OrderStatus.SHIPPED);

        order.setOrderStatus(Order.OrderStatus.DELIVERED);
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN') or @orderSecurity.isOrderOwner(#orderId, authentication.name)")
    public OrderResponse cancelOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);

        validateOrderStatus(order, Order.OrderStatus.PENDING, Order.OrderStatus.PROCESSING);

        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SELLER') or @orderSecurity.isOrderOwner(#orderId, authentication.name)")
    public OrderResponse returnOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        validateOrderStatus(order, Order.OrderStatus.DELIVERED);

        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();
            product.setStockQuantity(product.getStockQuantity() + detail.getQuantity());
            productRepository.save(product);
        }

        order.setOrderStatus(Order.OrderStatus.RETURNED);
        order.setPaymentStatus(Order.PaymentStatus.UNPAID);

        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    private void validateOrderStatus(Order order, Order.OrderStatus... expectedStatuses) {
        for (Order.OrderStatus status : expectedStatuses) {
            if (order.getOrderStatus() == status) {
                return;
            }
        }
        throw new AppException(ErrException.ORDER_STATUS_INVALID);
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrException.ORDER_NOT_EXISTED));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserNameAndDeletedFalse(username)
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_EXISTED));
    }
}
