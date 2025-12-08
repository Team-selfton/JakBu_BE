package com.jakbu.service;

import com.jakbu.domain.User;
import com.jakbu.dto.AuthResponse;
import com.jakbu.dto.KakaoTokenResponse;
import com.jakbu.dto.KakaoUserInfoResponse;
import com.jakbu.repository.UserRepository;
import com.jakbu.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
@Transactional
public class KakaoOAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final WebClient webClient;
    
    @Value("${kakao.oauth.client-id}")
    private String clientId;
    
    @Value("${kakao.oauth.client-secret}")
    private String clientSecret;
    
    @Value("${kakao.oauth.redirect-uri}")
    private String redirectUri;
    
    @Value("${kakao.oauth.token-uri}")
    private String tokenUri;
    
    @Value("${kakao.oauth.user-info-uri}")
    private String userInfoUri;

    public KakaoOAuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.webClient = WebClient.builder()
                .build();
    }

    /**
     * 인가 코드로 카카오 액세스 토큰 발급
     * 
     * @param code 카카오 인가 코드
     * @return KakaoTokenResponse
     */
    public KakaoTokenResponse requestToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        try {
            KakaoTokenResponse response = webClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();

            if (response == null || response.accessToken() == null) {
                throw new RuntimeException("Failed to get Kakao access token");
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Kakao access token: " + e.getMessage());
        }
    }

    /**
     * 액세스 토큰으로 카카오 사용자 정보 조회
     * 
     * @param accessToken 카카오 액세스 토큰
     * @return KakaoUserInfoResponse
     */
    public KakaoUserInfoResponse requestUserInfo(String accessToken) {
        try {
            KakaoUserInfoResponse response = webClient.get()
                    .uri(userInfoUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserInfoResponse.class)
                    .block();

            if (response == null || response.id() == null) {
                throw new RuntimeException("Failed to get Kakao user info");
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Kakao user info: " + e.getMessage());
        }
    }

    /**
     * 카카오 사용자 정보로 로그인 또는 회원가입 처리
     * email, nickname 기반으로 회원 조회 후 없으면 새 User 생성
     * JWT 생성 후 반환
     * 
     * @param kakaoUserInfo 카카오 사용자 정보
     * @return AuthResponse (JWT 토큰 포함)
     */
    public AuthResponse processLogin(KakaoUserInfoResponse kakaoUserInfo) {
        Long kakaoId = kakaoUserInfo.id();
        String nickname = kakaoUserInfo.getNickname();
        String email = kakaoUserInfo.getEmail();

        // 닉네임이 없으면 기본값 설정
        if (nickname == null || nickname.isEmpty()) {
            nickname = "카카오사용자" + kakaoId;
        }

        // 1. 카카오 ID로 사용자 조회
        Optional<User> existingUserByKakaoId = userRepository.findByKakaoId(kakaoId);
        
        // 2. 이메일이 있으면 이메일로도 조회
        Optional<User> existingUserByEmail = Optional.empty();
        if (email != null && !email.isEmpty()) {
            existingUserByEmail = userRepository.findByEmail(email);
        }

        User user;
        if (existingUserByKakaoId.isPresent()) {
            // 카카오 ID로 기존 사용자 찾음 - 로그인
            user = existingUserByKakaoId.get();
        } else if (existingUserByEmail.isPresent()) {
            // 이메일로 기존 사용자 찾음 - 카카오 ID 연결
            user = existingUserByEmail.get();
            // 카카오 ID가 없으면 연결
            if (user.getKakaoId() == null) {
                user = User.linkKakaoAccount(user, kakaoId);
                user = userRepository.save(user);
            }
        } else {
            // 신규 사용자 - 자동 회원가입
            user = User.createKakaoUser(kakaoId, nickname, email);
            user = userRepository.save(user);
        }

        // JWT 토큰 발급 (userId 기반)
        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getName());
    }
}

