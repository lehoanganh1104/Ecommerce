package com.example.demo.service;

import com.example.demo.config.JwtProperties;
import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtProperties jwtProperties;

    private String createToken(User user){
        String accessKey = jwtProperties.getAccessKey();
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUserName())
                .issuer("stinggggzz")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope", buildScopeToRole(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(accessKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e){
            log.error("can't create token", e);
            throw new RuntimeException(e);
        }
    }

    public AuthenticationResponse login(AuthenticationRequest authenticationRequest){
        var user = userRepository.findByUserName(authenticationRequest.getUserName())
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_EXISTED));
        boolean checkPassword = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if (!checkPassword)
            throw new AppException(ErrException.PASSWORD_NOT_MATCH);
        var token = createToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .check(true)
                .build();
    }

    public String buildScopeToRole(User user) {
        if (user.getRole() != null) {
            return user.getRole().name();
        }
        return "";
    }
}
