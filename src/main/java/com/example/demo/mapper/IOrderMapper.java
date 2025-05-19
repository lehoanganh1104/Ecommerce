package com.example.demo.mapper;

import com.example.demo.dto.response.OrderResponse;
import com.example.demo.model.Order;
import com.example.demo.model.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface IOrderMapper {
    @Mapping(source = "orderStatus", target = "orderStatus", qualifiedByName = "enumToString")
    @Mapping(source = "paymentStatus", target = "paymentStatus", qualifiedByName = "enumToString")
    @Mapping(source = "orderDetails", target = "orderDetails")
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "shipper.id", target = "shipperId")
    OrderResponse toOrderResponse(Order order);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderResponse.OrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail);

    List<OrderResponse.OrderDetailResponse> toOrderDetailResponseList(Set<OrderDetail> orderDetails);

    @Named("enumToString")
    default String enumToString(Enum<?> e) {
        return e == null ? null : e.name();
    }
}
