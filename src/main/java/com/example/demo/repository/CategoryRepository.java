package com.example.demo.repository;

import com.example.demo.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findAllByDeletedFalse(Pageable pageable);
    Page<Category> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
    Optional<Category> findByIdAndDeletedFalse(Long id);
    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);
}
