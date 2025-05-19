package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ProductImageResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.IProductImageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/product-images")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductImageController {
    IProductImageService productImageService;

    @PostMapping("/create/{productId}")
    public ResponseEntity<?> createProductImage(@PathVariable Long productId, @RequestParam("file") MultipartFile file) {
        try {
            ProductImageResponse response = productImageService.createProductImage(productId, file);
            ApiResponse<ProductImageResponse> apiResponse = ApiResponse.success(response);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex) {
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductImageById(@PathVariable Long id) {
        try {
            ProductImageResponse response = productImageService.getProductImageById(id);
            ApiResponse<ProductImageResponse> apiResponse = ApiResponse.success(response);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex) {
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProductImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ProductImageResponse> response = productImageService.getAllProductImages(pageable);
            ApiResponse<Page<ProductImageResponse>> apiResponse = ApiResponse.success(response);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex){
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long id) {
        try {
            productImageService.deleteProductImage(id);
            ApiResponse<String> apiResponse = ApiResponse.success(null);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex) {
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }
}
