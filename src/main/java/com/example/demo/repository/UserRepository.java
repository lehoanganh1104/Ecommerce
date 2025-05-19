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
    Page<User> findByUserNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
    Optional<User> findByIdAndDeletedFalse(Long id);
    Optional<User> findByUserName(String userName);
    boolean existsByEmailAndDeletedFalse(String email);
    boolean existsByPhoneNumberAndDeletedFalse(String phoneNumber);
    Optional<User> findByUserNameAndDeletedFalse(String userName);
    boolean existsByUserNameIgnoreCaseAndDeletedFalse(String userName);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phone);
}
