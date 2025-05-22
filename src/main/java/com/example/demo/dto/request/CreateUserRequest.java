package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CreateUserRequest {
    @NotBlank(message = "USERNAME_MUST_NOT_BE_BLANK")
    @Size(min = 3, message = "INVALID_USERNAME")
    private String username;
    @Size(min = 10, message = "INVALID_EMAIL")
    @NotBlank(message = "EMAIL_MUST_NOT_BE_BLANK")
    private String email;
    @NotBlank(message = "PASSWORD_MUST_NOT_BE_BLANK")
    @Size(min = 6, max = 72, message = "INVALID_PASSWORD")
    private String password;
    private String fullName;
    @NotBlank(message = "PHONE_NUMBER_MUST_NOT_BE_BLANK")
    @Size(min = 10, message = "INVALID_PHONE_NUMBER")
    private String phoneNumber;
    private String address;
}
