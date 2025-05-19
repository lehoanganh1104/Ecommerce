package com.example.demo.controller;

import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.impl.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/authentication")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> createToken(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            var response = authenticationService.login(authenticationRequest);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AppException ex) {
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }
}
