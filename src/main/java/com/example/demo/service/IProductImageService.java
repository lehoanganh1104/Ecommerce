package com.example.demo.service;

import com.example.demo.dto.response.ProductImageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IProductImageService {
    ProductImageResponse uploadProductImage(Long productId, MultipartFile file);
    ProductImageResponse getProductImageById(Long id);
    Page<ProductImageResponse> getAllProductImages(Pageable pageable);
    void deleteProductImage(Long id);
}
