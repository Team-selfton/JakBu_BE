package com.jakbu.controller;

import com.jakbu.dto.AuthRequest;
import com.jakbu.dto.AuthResponse;
import com.jakbu.dto.LoginRequest;
import com.jakbu.service.AuthService;
import com.jakbu.service.KakaoOAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final KakaoOAuthService kakaoOAuthService;
    
    @Value("${kakao.oauth.client-id}")
    private String clientId;
    
    @Value("${kakao.oauth.redirect-uri}")
    private String redirectUri;
    
    @Value("${kakao.oauth.authorization-uri}")
    private String authorizationUri;

    public AuthController(AuthService authService, KakaoOAuthService kakaoOAuthService) {
        this.authService = authService;
        this.kakaoOAuthService = kakaoOAuthService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 카카오 OAuth 로그인 시작
     * 카카오 인증 페이지로 리다이렉트합니다.
     */
    @GetMapping("/kakao/login")
    public RedirectView kakaoLogin() {
        String kakaoAuthUrl = String.format(
                "%s?response_type=code&client_id=%s&redirect_uri=%s",
                authorizationUri,
                clientId,
                redirectUri
        );
        return new RedirectView(kakaoAuthUrl);
    }

    /**
     * 카카오 OAuth 콜백 처리
     * 인가 코드를 받아서 토큰 발급, 사용자 정보 조회, 로그인/회원가입 처리 후 JWT를 반환합니다.
     * 
     * @param code 카카오에서 전달받은 인가 코드
     * @param error 에러 코드 (카카오 로그인 취소 시)
     * @return JWT 토큰이 포함된 AuthResponse
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<AuthResponse> kakaoCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error) {
        
        if (error != null) {
            throw new RuntimeException("Kakao OAuth error: " + error);
        }
        
        if (code == null || code.isEmpty()) {
            throw new RuntimeException("Authorization code is missing");
        }

        // 1. 인가 코드로 액세스 토큰 발급
        var tokenResponse = kakaoOAuthService.requestToken(code);
        
        // 2. 액세스 토큰으로 사용자 정보 조회
        var userInfo = kakaoOAuthService.requestUserInfo(tokenResponse.accessToken());
        
        // 3. 사용자 로그인/회원가입 처리 및 JWT 발급
        AuthResponse authResponse = kakaoOAuthService.processLogin(userInfo);
        
        return ResponseEntity.ok(authResponse);
    }
}


