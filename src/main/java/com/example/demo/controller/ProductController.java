package com.example.demo.controller;

import com.example.demo.common.constants.SuccessMessage;
import com.example.demo.dto.request.CreateProductRequest;
import com.example.demo.dto.request.UpdateProductRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ProductResponse;
import com.example.demo.service.IProductService;
import jakarta.validation.Valid;
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
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    IProductService productService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest request){
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message(SuccessMessage.PRODUCT_CREATED)
                .data(response)
                .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllCategories(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> responses = productService.getAllProduct(search, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ProductResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.PRODUCTS_FETCHED)
                .data(responses)
                .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.PRODUCT_FETCHED)
                .data(response)
                .build()
        );
    }

    @PostMapping("/upload-image/{productId}")
    public ResponseEntity<ApiResponse<Void>> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        productService.uploadProductImage(productId, file);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.PRODUCT_IMAGE_UPLOADED)
                .data(null)
                .build());
    }

    @GetMapping("/get-by-category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> responses = productService.getProductByCategoryId(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ProductResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.PRODUCTS_FETCHED)
                .data(responses)
                .build()
        );
    }

    @PutMapping("update/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.PRODUCT_UPDATED)
                .data(response)
                .build()
        );
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.PRODUCT_DELETED)
                .data(null)
                .build()
        );
    }
}
