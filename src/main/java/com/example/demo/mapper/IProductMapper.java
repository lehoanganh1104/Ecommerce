package com.example.demo.mapper;

import com.example.demo.dto.request.CreateProductRequest;
import com.example.demo.dto.response.ProductResponse;
import com.example.demo.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IProductMapper {
    @Mapping(source = "category.id", target = "categoryId")
    ProductResponse toProductResponse(Product product);

    Product toProduct(CreateProductRequest request);
}
