package com.example.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrException {
    // General errors (1000–1099)
    ERR_EXCEPTION(1000, "Unexpected error occurred"),
    INVALID_KEY(1001, "Invalid message key"),
    STRIPE_INTEGRATION_ERROR(1002, "Stripe integration error"),
    TOKEN_NOT_FOUND(1003, "Token not found"),
    TOKEN_INVALID(1700, "Invalid token"),

    // User errors (1100–1199)
    USER_ALREADY_EXISTS(1100, "User already exists"),
    USER_NOT_FOUND(1101, "User not found"),
    USERNAME_MUST_NOT_BE_BLANK(1102, "Username must not be blank"),
    USERNAME_TOO_SHORT(1103, "Username must be at least 3 characters"),
    PASSWORD_MUST_NOT_BE_BLANK(1104, "Password must not be blank"),
    PASSWORD_TOO_SHORT(1105, "Password must be at least 4 characters"),
    PASSWORD_NOT_MATCH(1106, "Passwords do not match"),
    PHONE_NUMBER_TOO_SHORT(1107, "Phone number must be at least 10 characters"),
    PHONE_NUMBER_MUST_NOT_BE_BLANK(1108, "Phone number must not be blank"),
    EMAIL_MUST_NOT_BE_BLANK(1109, "Email must not be blank"),
    EMAIL_TOO_SHORT(1110, "Email must be at least 10 characters"),
    EMAIL_ALREADY_USED(1111, "Email is already used"),
    PHONE_NUMBER_ALREADY_USED(1112, "Phone number is already used"),
    USER_ROLE_INVALID(1113, "User role is invalid"),
    USER_NOT_SHIPPER(1114, "User is not a shipper"),

    // Category errors (1200–1299)
    CATEGORY_ALREADY_EXISTS(1200, "Category name already exists"),
    CATEGORY_NOT_FOUND(1201, "Category not found"),
    CATEGORY_NAME_BLANK(1202, "Category name must not be blank"),
    CATEGORY_NAME_TOO_SHORT(1203, "Category name must be at least 3 characters"),

    // Product errors (1300–1399)
    PRODUCT_ALREADY_EXISTS(1300, "Product name already exists"),
    PRODUCT_NOT_FOUND(1301, "Product not found"),
    PRODUCT_NAME_BLANK(1302, "Product name must not be blank"),
    PRODUCT_PRICE_INVALID(1303, "Product price must be greater than zero"),
    PRODUCT_STOCK_INVALID(1304, "Product stock quantity cannot be negative"),
    PRODUCT_CATEGORY_NOT_FOUND(1305, "Category not found for product"),
    PRODUCT_SELLER_NOT_FOUND(1306, "Seller not found for product"),
    PRODUCT_QUANTITY_INVALID(1307, "Invalid quantity for product"),
    PRODUCT_OUT_OF_STOCK(1308, "Product is out of stock"),
    PRODUCT_IMAGE_NOT_FOUND(1309, "Product image not found"),

    // Order errors (1400–1499)
    ORDER_NOT_FOUND(1400, "Order not found"),
    ORDER_STATUS_INVALID(1401, "Invalid order status"),
    ORDER_ALREADY_PAID(1402, "Order has already been paid"),
    ORDER_ID_NULL(1403, "Order ID must not be null"),
    ORDER_ALREADY_ASSIGNED_SHIPPER(1404, "Order already assigned to another shipper"),

    // Payment errors (1500–1599)
    PAYMENT_NOT_FOUND(1500, "Payment not found"),
    PAYMENT_PROCESSING_FAILED(1501, "Payment processing failed"),
    PAYMENT_NOT_COMPLETED(1502, "Payment not completed"),
    PAYMENT_INTENT_ID_NULL(1503, "PaymentIntent ID must not be null"),
    PAYMENT_METHOD_NOT_SUPPORTED(1504, "Unsupported payment method"),
    VNPAY_MISSING_SECURE_HASH(1505, "Missing vnp_SecureHash"),
    VNPAY_CHECKSUM_INVALID(1506, "Checksum verification failed"),
    VNPAY_INVALID_MERCHANT_CODE(1507, "Invalid merchant code"),
    VNPAY_AMOUNT_MISMATCH(1508, "Amount mismatch"),
    INVALID_SIGNATURE(1509, "Invalid signature"),

    // File errors (1600–1699)
    FILE_NOT_PROVIDED(1600, "No file provided"),
    DIRECTORY_CREATION_FAILED(1601, "Failed to create directory"),
    FILE_STORE_FAILED(1602, "Failed to store file"),

    // Token errors (1700–1799)

;
    private int code;
    private String message;
}
