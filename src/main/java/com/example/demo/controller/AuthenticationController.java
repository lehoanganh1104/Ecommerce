package com.example.demo.controller;

import com.example.demo.constants.SuccessMessage;
import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.RefreshTokenRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.exception.ErrException;
import com.example.demo.service.impl.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/authentication")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> createToken(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        var response = authenticationService.login(authenticationRequest);
        return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message(SuccessMessage.TOKEN_CREATED)
                .data(response)
                .build()
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthenticationResponse response = authenticationService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.<AuthenticationResponse>builder()
                        .status(200)
                        .message(SuccessMessage.TOKEN_REFRESHED)
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Void>builder()
                            .status(ErrException.TOKEN_INVALID.getCode())
                            .message(ErrException.TOKEN_INVALID.getMessage())
                            .data(null)
                            .build()
            );
        }

        String accessToken = authorizationHeader.substring(7);
        authenticationService.logout(accessToken);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.USER_LOGGED_OUT)
                .data(null)
                .build()
        );
    }
}
