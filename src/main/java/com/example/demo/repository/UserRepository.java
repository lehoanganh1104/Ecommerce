package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByDeletedFalse(Pageable pageable);
    Page<User> findByUsernameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
    Optional<User> findByUsernameIgnoreCaseAndDeletedFalse(String username);
    Optional<User> findByIdAndDeletedFalse(Long id);
    Optional<User> findByUsername(String username);
    boolean existsByEmailAndDeletedFalse(String email);
    boolean existsByPhoneNumberAndDeletedFalse(String phoneNumber);
    Optional<User> findByUsernameAndDeletedFalse(String username);
    boolean existsByUsernameIgnoreCaseAndDeletedFalse(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phone);
}
