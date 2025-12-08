package com.jakbu.dto;

public record AuthResponse(
        String token,
        Long userId,
        String name
) {
}

