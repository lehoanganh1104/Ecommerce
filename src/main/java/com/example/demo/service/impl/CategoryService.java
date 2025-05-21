package com.example.demo.service.impl;

import com.example.demo.dto.request.CreateCategoryRequest;
import com.example.demo.dto.request.UpdateCategoryRequest;
import com.example.demo.dto.response.CategoryResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.mapper.ICategoryMapper;
import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.ICategoryService;
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
public class CategoryService implements ICategoryService {
    CategoryRepository categoryRepository;
    ICategoryMapper categoryMapper;

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())){
            throw new AppException(ErrException.CATEGORY_ALREADY_EXISTS);
        }

        Category category = categoryMapper.toCategory(request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())
                && !category.getName().equalsIgnoreCase(request.getName())) {
            throw new AppException(ErrException.CATEGORY_ALREADY_EXISTS);
        }

        category.setName(request.getName());
        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @Override
    public Page<CategoryResponse> getAllCategories(String search, Pageable pageable) {
        Page<Category> categories;

        if (search == null || search.isBlank()){
            categories = categoryRepository.findAllByDeletedFalse(pageable);
        } else {
            categories = categoryRepository.findByNameContainingIgnoreCaseAndDeletedFalse(search, pageable);
        }
        return categories.map(categoryMapper::toCategoryResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.CATEGORY_NOT_FOUND));
        category.setDeleted(true);
        categoryRepository.save(category);
    }
}
