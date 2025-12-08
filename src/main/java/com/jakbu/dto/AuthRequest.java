package com.jakbu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,
        
        @NotBlank(message = "Password is required")
        String password,
        
        @NotBlank(message = "Name is required")
        String name
) {
}

