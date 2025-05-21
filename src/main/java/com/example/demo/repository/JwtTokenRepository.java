package com.example.demo.repository;

import com.example.demo.model.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    Optional<JwtToken> findByAccessTokenAndDeletedFalse(String token);
    Optional<JwtToken> findByRefreshTokenAndDeletedFalse(String refreshToken);
}
