package com.example.demo.controller;

import com.example.demo.constants.SuccessMessage;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ProductImageResponse;
import com.example.demo.service.IProductImageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<ProductImageResponse>> createProductImage(@PathVariable Long productId, @RequestParam("file") MultipartFile file) {
        ProductImageResponse response = productImageService.uploadProductImage(productId, file);
        return ResponseEntity.ok(ApiResponse.<ProductImageResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message(SuccessMessage.FILE_UPLOADED)
                .data(response)
                .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductImageResponse>> getProductImageById(@PathVariable Long id) {
        ProductImageResponse response = productImageService.getProductImageById(id);
        return ResponseEntity.ok(ApiResponse.<ProductImageResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.FILE_FETCHED)
                .data(response)
                .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductImageResponse>>> getAllProductImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductImageResponse> response = productImageService.getAllProductImages(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ProductImageResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.FILES_FETCHED)
                .data(response)
                .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProductImage(@PathVariable Long id) {
        productImageService.deleteProductImage(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.FILE_DELETED)
                .data(null)
                .build()
        );
    }
}
