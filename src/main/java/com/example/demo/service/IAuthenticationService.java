package com.example.demo.service;

import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.RefreshTokenRequest;
import com.example.demo.dto.response.AuthenticationResponse;

public interface IAuthenticationService {
    AuthenticationResponse login(AuthenticationRequest authenticationRequest);
    AuthenticationResponse refreshToken(RefreshTokenRequest request);
    void logout(String accessToken);
}
