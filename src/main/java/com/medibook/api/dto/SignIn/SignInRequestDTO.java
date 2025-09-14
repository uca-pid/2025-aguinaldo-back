package com.medibook.api.dto.SignIn;

import jakarta.validation.constraints.*;

public record SignInRequestDTO(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    String password
) {}