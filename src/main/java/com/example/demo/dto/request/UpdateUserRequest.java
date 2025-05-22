package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateUserRequest {
    @Email(message = "INVALID_EMAIL")
    private String email;
    @Size(min = 6, max = 72, message = "INVALID_PASSWORD")
    private String password;
    private String fullName;
    @Size(min = 10, message = "INVALID_PHONE_NUMBER")
    private String phoneNumber;
    private String address;
}
