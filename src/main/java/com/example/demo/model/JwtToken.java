package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "jwt_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "access_token", length = 512, nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", length = 512, nullable = false)
    private String refreshToken;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "access_token_expired_at")
    private Instant accessTokenExpiredAt;

    @Column(name = "refresh_token_expired_at")
    private Instant refreshTokenExpiredAt;

    @Column(name = "deleted")
    private Boolean deleted = false;
}
