package com.example.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrException {
    ERR_EXCEPTION(99, "Err exception"),
    USER_EXISTED(101, "User existed"),
    USER_NOT_EXISTED(102, "User does not exist"),
    INVALID_USERNAME(103, "Username must be at least 3 characters"),
    INVALID_PASSWORD(104, "Password must be at least 6 characters"),
    PASSWORD_NOT_MATCH(105, "Password not match"),
    INVALID_PHONE_NUMBER(106, "Phone number must be at least 10 characters"),
    INVALID_EMAIL(107, "Email must be at least 10 characters"),
    EMAIL_EXISTED(108, "Email is already used"),
    PHONE_NUMBER_EXISTED(109, "Phone number is already used"),
    USER_INVALID_ROLE(110, "User role is invalid"),
    USER_NOT_SHIPPER(111, "User is not a shipper"),
    INVALID_KEY(112, "Invalid message key err"),

    // Category errors
    CATEGORY_EXISTED(201, "Category name already exists"),
    CATEGORY_NOT_EXISTED(202, "Category not found"),
    CATEGORY_INVALID_NAME(203, "Category name must not be blank"),
    CATEGORY_NAME_TOO_SHORT(204, "Category name must be at least 3 characters"),

    // Product errors
    PRODUCT_EXISTED(301, "Product name already exists"),
    PRODUCT_NOT_EXISTED(302, "Product does not exist"),
    PRODUCT_INVALID_NAME(303, "Product name must not be blank"),
    PRODUCT_INVALID_PRICE(304, "Product price must be greater than zero"),
    PRODUCT_INVALID_STOCK(305, "Product stock quantity cannot be negative"),
    PRODUCT_CATEGORY_NOT_FOUND(306, "Category not found for product"),
    PRODUCT_SELLER_NOT_FOUND(307, "Seller not found for product"),
    INVALID_QUANTITY(308, "Invalid quantity for product"),
    PRODUCT_OUT_OF_STOCK(309, "Product is out of stock"),
    PRODUCT_IMAGE_NOT_EXISTED(310,"Product image not exist"),

    // Order errors
    ORDER_NOT_EXISTED(401, "Order does not exist"),
    ORDER_STATUS_INVALID(402, "Invalid order status"),
    ORDER_ALREADY_PAID(403, "Order already paid"),
    ORDER_ID_NULL(404, "OrderId null"),
    ORDER_ALREADY_HAVE_SHIPPER(405,"Order already assigned to another shipper"),

    // Payment erors
    PAYMENT_NOT_EXISTED(401, "Order does not exist"),
    PAYMENT_PROCESSING_ERROR(402, "Payment processing error"),
    PAYMENT_NOT_COMPLETED(403, "Payment not complete"),
    PAYMENT_INTENT_ID_NULL(404,"PaymentIntentId null"),

    // File
    NOT_FILE(405, "Not file"),
    DIRECTORY_CREATION_FAILED(406,"Directory create fail"),
    FILE_STORE_FAILED(407, "File store fail"),
    STRIPE_INTEGRATION_ERROR(98, "Stripe Err"),






;
    private int code;
    private String message;
}
