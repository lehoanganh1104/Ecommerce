package com.example.demo.controller;

import com.example.demo.dto.request.CreateCategoryRequest;
import com.example.demo.dto.request.UpdateCategoryRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.CategoryResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.ICategoryService;
import com.example.demo.service.impl.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    ICategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<?> createCategory(@RequestBody @Valid CreateCategoryRequest request){
        try {
            CategoryResponse response = categoryService.createCategory(request);
            ApiResponse<CategoryResponse> apiResponse = ApiResponse.success(response);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex) {
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CategoryResponse> response = categoryService.getAllCategories(search, pageable);
            ApiResponse<Page<CategoryResponse>> apiResponse = ApiResponse.success(response);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex) {
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        try {
            CategoryResponse response = categoryService.updateCategory(id, request);
            ApiResponse<CategoryResponse> apiResponse = ApiResponse.success(response);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex) {
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (AppException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage()));
        }
    }


}
