package com.example.demo.mapper;

import com.example.demo.dto.response.ProductImageResponse;
import com.example.demo.model.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IProductImageMapper {
    @Mapping(source = "product.id", target = "productId")
    ProductImageResponse toProductImageResponse(ProductImage productImage);
}
