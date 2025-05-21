package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CreateUserRequest {
    @Size(min = 3, message = "INVALID_USERNAME")
    private String userName;
    @Size(min = 10, message = "INVALID_EMAIL")
    private String email;
    @Size(min = 6, max = 72, message = "INVALID_PASSWORD")
    private String password;
    private String fullName;
    @Size(min = 10, message = "INVALID_PHONE_NUMBER")
    private String phoneNumber;
    private String address;
}
