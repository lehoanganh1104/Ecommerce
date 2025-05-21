package com.example.demo.config;

import com.example.demo.repository.JwtTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final JwtTokenRepository jwtTokenRepository;
    private final JwtAuthenticationConverter delegateConverter;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String tokenValue = jwt.getTokenValue();
        if (tokenValue == null || tokenValue.isEmpty()) {
            throw new BadCredentialsException("JWT token is missing.");
        }

        if (isTokenDeleted(tokenValue)) {
            throw new BadCredentialsException("Token has been revoked or deleted.");
        }

        return delegateConverter.convert(jwt);
    }

    private boolean isTokenDeleted(String tokenValue) {
        return jwtTokenRepository.findByAccessTokenAndDeletedFalse(tokenValue).isEmpty();
    }
}
