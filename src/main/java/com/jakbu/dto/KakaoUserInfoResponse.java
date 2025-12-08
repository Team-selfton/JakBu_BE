package com.jakbu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record KakaoUserInfoResponse(
        Long id,
        
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            Profile profile,
            String email
    ) {
    }
    
    public record Profile(
            String nickname,
            @JsonProperty("profile_image_url")
            String profileImageUrl
    ) {
    }
    
    public String getNickname() {
        return kakaoAccount != null && kakaoAccount.profile != null 
                ? kakaoAccount.profile.nickname 
                : null;
    }
    
    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.email : null;
    }
}

