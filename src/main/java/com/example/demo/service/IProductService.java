package com.example.demo.service;

import com.example.demo.dto.request.CreateProductRequest;
import com.example.demo.dto.request.UpdateProductRequest;
import com.example.demo.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(Long id, UpdateProductRequest request);
    Page<ProductResponse> getAllProduct(String search, Pageable pageable);
    Page<ProductResponse> getProductByCategoryId(Long categoryId, Pageable pageable);
    ProductResponse getProductById(Long id);
    void deleteProduct(Long id);
    void uploadProductImage(Long productId, MultipartFile file);
}
