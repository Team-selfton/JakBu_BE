package com.jakbu.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Account ID is required")
        String accountId,
        
        @NotBlank(message = "Password is required")
        String password
) {
}

