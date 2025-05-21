package com.example.demo.service.impl;

import com.example.demo.dto.response.ProductImageResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.mapper.IProductImageMapper;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.repository.ProductImageRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.IProductImageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductImageService implements IProductImageService {
    ProductRepository productRepository;
    ProductImageRepository productImageRepository;
    FileService fileService;
    IProductImageMapper productImageMapper;

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ProductImageResponse uploadProductImage(Long productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrException.PRODUCT_NOT_FOUND));

        String fileName = fileService.storeImage(file, "products");

        ProductImage productImage = ProductImage.builder()
                .product(product)
                .imageUrl(fileName)
                .deleted(false)
                .build();

        ProductImage savedImage = productImageRepository.save(productImage);

        return productImageMapper.toProductImageResponse(savedImage);
    }

    @Override
    public ProductImageResponse getProductImageById(Long id) {
        ProductImage productImage = productImageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrException.PRODUCT_IMAGE_NOT_FOUND));
        return productImageMapper.toProductImageResponse(productImage);
    }

    @Override
    public Page<ProductImageResponse> getAllProductImages(Pageable pageable) {
        Page<ProductImage> page = productImageRepository.findAllByDeletedFalse(pageable);
        return page.map(productImageMapper::toProductImageResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public void deleteProductImage(Long id) {
        ProductImage productImage = productImageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrException.PRODUCT_IMAGE_NOT_FOUND));
        productImage.setDeleted(true);
        productImageRepository.save(productImage);
    }
}
