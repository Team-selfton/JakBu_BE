package com.jakbu.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String name
) {
    /**
     * 하위 호환성을 위한 생성자 (기존 코드에서 사용)
     */
    public AuthResponse(String token, Long userId, String name) {
        this(token, null, userId, name);
    }
}

