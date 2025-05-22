package com.example.demo.service.impl;

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
import com.example.demo.service.IProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService implements IProductService {
    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    IProductMapper productMapper;
    FileService fileService;

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())){
            throw new AppException(ErrException.PRODUCT_ALREADY_EXISTS);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrException.PRODUCT_CATEGORY_NOT_FOUND));

        Product product = productMapper.toProduct(request);
        product.setCategory(category);
        product.setImageUrl(null);

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.PRODUCT_NOT_FOUND));

        if (request.getName() != null && !request.getName().isBlank()
                && !product.getName().equalsIgnoreCase(request.getName())) {
            if (productRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {
                throw new AppException(ErrException.PRODUCT_ALREADY_EXISTS);
            }
            product.setName(request.getName());
        }

        if (request.getDescription() != null && !request.getDescription().equals(product.getDescription())) {
            product.setDescription(request.getDescription());
        }

        if (request.getPrice() != null && product.getPrice().compareTo(request.getPrice()) != 0) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrException.PRODUCT_PRICE_INVALID);
            }
            product.setPrice(request.getPrice());
        }

        if (request.getStockQuantity() != null && !request.getStockQuantity().equals(product.getStockQuantity())) {
            if (request.getStockQuantity() < 0) {
                throw new AppException(ErrException.PRODUCT_STOCK_INVALID);
            }
            product.setStockQuantity(request.getStockQuantity());
        }

        // categoryId nullable, nếu khác null và khác thì cập nhật
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory().getId())) {
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

    @Override
    public Page<ProductResponse> getProductByCategoryId(Long categoryId, Pageable pageable) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrException.CATEGORY_NOT_FOUND));
        Page<Product> products= productRepository.findByCategoryId(categoryId, pageable);
        return products.map(productMapper::toProductResponse);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.PRODUCT_NOT_FOUND));
        return productMapper.toProductResponse(product);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.PRODUCT_NOT_FOUND));
        product.setDeleted(true);
        productRepository.save(product);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public void uploadProductImage(Long productId, MultipartFile file) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new AppException(ErrException.PRODUCT_NOT_FOUND));

        String newImage = fileService.replaceImage(product.getImageUrl(), file, "products");
        product.setImageUrl(newImage);

        productRepository.save(product);
    }
}
