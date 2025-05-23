package com.example.demo.mapper;

import com.example.demo.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IPaymentMapper {
    @Mapping(source = "order.id", target = "orderId")
    PaymentResponse toPaymentResponse(Payment payment);
}
