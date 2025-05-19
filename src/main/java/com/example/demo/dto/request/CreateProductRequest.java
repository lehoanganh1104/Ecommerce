package com.example.demo.dto.request;

import com.example.demo.model.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {
    @NotBlank(message = "PRODUCT_INVALID_NAME")
    private String name;

    private String description;

    @NotNull(message = "PRODUCT_INVALID_PRICE")
    @DecimalMin(value = "0.0", inclusive = false, message = "PRODUCT_INVALID_PRICE")
    private BigDecimal price;

    @NotNull(message = "PRODUCT_INVALID_STOCK")
    @Min(value = 0, message = "PRODUCT_INVALID_STOCK")
    private Integer stockQuantity;

    @NotNull(message = "PRODUCT_CATEGORY_NOT_FOUND")
    private Long categoryId;
}
