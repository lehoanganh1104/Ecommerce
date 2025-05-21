package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @Size(min = 10, message = "INVALID_EMAIL")
    private String email;
    @Size(min = 6, max = 72, message = "INVALID_PASSWORD")
    private String password;
    private String fullName;
    @Size(min = 10, message = "INVALID_PHONE_NUMBER")
    private String phoneNumber;
    private String address;
}
