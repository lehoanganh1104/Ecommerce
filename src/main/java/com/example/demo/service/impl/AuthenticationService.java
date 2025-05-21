package com.example.demo.service.impl;

import com.example.demo.config.JwtProperties;
import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.RefreshTokenRequest;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.model.JwtToken;
import com.example.demo.model.User;
import com.example.demo.repository.JwtTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.IAuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService implements IAuthenticationService {
    UserRepository userRepository;
    JwtTokenRepository jwtTokenRepository;
    PasswordEncoder passwordEncoder;
    JwtProperties jwtProperties;

    public AuthenticationResponse login(AuthenticationRequest authenticationRequest){
        var user = userRepository.findByUserName(authenticationRequest.getUserName())
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_FOUND));

        if (!passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword())) {
            throw new AppException(ErrException.PASSWORD_NOT_MATCH);
        }

        long accessExp = jwtProperties.getExpirationAccess();
        long refreshExp = jwtProperties.getExpirationRefresh();
        Instant now = Instant.now();

        String accessToken = generateToken(user, accessExp, jwtProperties.getTypeAccess());
        String refreshToken = generateToken(user, refreshExp, jwtProperties.getTypeRefresh());

        JwtToken jwtToken = JwtToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .createdAt(now)
                .accessTokenExpiredAt(now.plusSeconds(accessExp))
                .refreshTokenExpiredAt(now.plusSeconds(refreshExp))
                .deleted(false)
                .build();
        jwtTokenRepository.save(jwtToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .check(true)
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        JwtToken oldToken = jwtTokenRepository.findByRefreshTokenAndDeletedFalse(refreshToken)
                .orElseThrow(() -> new AppException(ErrException.TOKEN_NOT_FOUND));

        if (oldToken.getRefreshTokenExpiredAt().isBefore(Instant.now())) {
            throw new AppException(ErrException.TOKEN_INVALID);
        }

        User user = userRepository.findById(oldToken.getUserId())
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_FOUND));

        long accessExp = jwtProperties.getExpirationAccess();
        String newAccessToken = generateToken(user, accessExp, jwtProperties.getTypeAccess());
        Instant accessTokenExpiredAt = Instant.now().plusSeconds(accessExp);

        oldToken.setDeleted(true);
        jwtTokenRepository.save(oldToken);

        JwtToken newToken = JwtToken.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .createdAt(Instant.now())
                .accessTokenExpiredAt(accessTokenExpiredAt)
                .refreshTokenExpiredAt(oldToken.getRefreshTokenExpiredAt())
                .deleted(false)
                .build();

        jwtTokenRepository.save(newToken);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logout(String accessToken) {
        JwtToken jwtToken = jwtTokenRepository.findByAccessTokenAndDeletedFalse(accessToken)
                .orElseThrow(() -> new AppException(ErrException.TOKEN_NOT_FOUND));
        jwtToken.setDeleted(true);
        jwtTokenRepository.save(jwtToken);
    }

    public String buildScopeToRole(User user) {
        if (user.getRole() != null) {
            return user.getRole().name();
        }
        return "";
    }

    private String generateToken(User user, long expirationSeconds, String type) {
        String secretKey = jwtProperties.getSecretKey();
        String issuer = jwtProperties.getIssuer();

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getUserName())
                .issuer(issuer)
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(expirationSeconds)))
                .claim("scope", buildScopeToRole(user))
                .claim("type", type)
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(claims.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(secretKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Failed to generate " + type + " token", e);
            throw new RuntimeException("Error generating " + type + " token", e);
        }
    }
}
