package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @NotBlank(message = "USERNAME_MUST_NOT_BE_BLANK")
    @Size(min = 3, message = "USERNAME_TOO_SHORT")
    String username;
    @NotBlank(message = "PASSWORD_MUST_NOT_BE_BLANK")
    @Size(min = 4, message = "PASSWORD_TOO_SHORT")
    String password;
}
