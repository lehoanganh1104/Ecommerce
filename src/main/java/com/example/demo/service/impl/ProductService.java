package com.example.demo.service;

import com.example.demo.dto.request.CreateProductRequest;
import com.example.demo.dto.request.UpdateProductRequest;
import com.example.demo.dto.response.ProductResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.mapper.IProductMapper;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService implements IProductService{
    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    IProductMapper productMapper;

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())){
            throw new AppException(ErrException.PRODUCT_EXISTED);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrException.PRODUCT_CATEGORY_NOT_FOUND));

        Product product = productMapper.toProduct(request);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.PRODUCT_NOT_EXISTED));

        if (productRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())
                && !product.getName().equalsIgnoreCase(request.getName())) {
            throw new AppException(ErrException.PRODUCT_EXISTED);
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findByIdAndDeletedFalse(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrException.PRODUCT_CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.save(product);

        return productMapper.toProductResponse(updatedProduct);
    }

    @Override
    public Page<ProductResponse> getAllProduct(String search, Pageable pageable) {
        Page<Product> products;

        if (search == null || search.isBlank()){
            products = productRepository.findAllByDeletedFalse(pageable);
        } else {
            products = productRepository.findByNameContainingIgnoreCaseAndDeletedFalse(search, pageable);
        }
        return products.map(productMapper::toProductResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.PRODUCT_NOT_EXISTED));
        product.setDeleted(true);
        productRepository.save(product);
    }
}
