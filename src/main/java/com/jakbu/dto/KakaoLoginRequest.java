package com.jakbu.dto;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
        @NotBlank(message = "Kakao access token is required")
        String accessToken
) {
}

