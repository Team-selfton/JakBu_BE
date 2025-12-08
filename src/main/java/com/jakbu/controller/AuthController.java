package com.jakbu.controller;

import com.jakbu.dto.AuthRequest;
import com.jakbu.dto.AuthResponse;
import com.jakbu.dto.KakaoLoginRequest;
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
     * 기존 방식: 클라이언트가 accessToken을 전달하는 방식 (하위 호환성 유지)
     */
    @PostMapping("/kakao")
    public ResponseEntity<AuthResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        AuthResponse response = authService.kakaoLogin(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 카카오 OAuth 로그인 시작
     * 클라이언트가 이 엔드포인트를 호출하면 카카오 로그인 페이지로 리다이렉트됩니다.
     */
    @GetMapping("/kakao/login")
    public RedirectView kakaoLoginRedirect() {
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
     * 카카오에서 인가 코드를 받아서 처리합니다.
     * 
     * @param code 카카오에서 전달받은 인가 코드
     * @param error 에러 코드 (카카오 로그인 취소 시)
     * @param redirect 리다이렉트 여부 (true면 리다이렉트, false면 JSON 응답)
     * @param redirectUrl 리다이렉트 URL (redirect=true일 때 사용, 기본값: http://localhost:3000/login)
     * @return JSON 응답 또는 리다이렉트
     */
    @GetMapping("/kakao/callback")
    public Object kakaoCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, defaultValue = "false") Boolean redirect,
            @RequestParam(required = false) String redirectUrl) {
        
        // 에러 처리
        if (error != null) {
            if (redirect) {
                String errorRedirectUrl = (redirectUrl != null ? redirectUrl : "http://localhost:3000/login") 
                        + "?error=" + error;
                return new RedirectView(errorRedirectUrl);
            }
            throw new RuntimeException("Kakao OAuth error: " + error);
        }
        
        if (code == null || code.isEmpty()) {
            if (redirect) {
                String errorRedirectUrl = (redirectUrl != null ? redirectUrl : "http://localhost:3000/login") 
                        + "?error=missing_code";
                return new RedirectView(errorRedirectUrl);
            }
            throw new RuntimeException("Authorization code is missing");
        }

        try {
            // 1. 인가 코드로 액세스 토큰 발급
            var tokenResponse = kakaoOAuthService.getAccessToken(code);
            
            // 2. 액세스 토큰으로 사용자 정보 조회
            var userInfo = kakaoOAuthService.getUserInfo(tokenResponse.accessToken());
            
            // 3. 사용자 로그인/회원가입 처리 및 JWT 발급
            AuthResponse authResponse = kakaoOAuthService.processKakaoLogin(userInfo);
            
            // 리다이렉트 모드인 경우
            if (redirect) {
                String finalRedirectUrl = (redirectUrl != null ? redirectUrl : "http://localhost:3000/login") 
                        + "?token=" + authResponse.token() 
                        + "&userId=" + authResponse.userId() 
                        + "&name=" + authResponse.name();
                return new RedirectView(finalRedirectUrl);
            }
            
            // JSON 응답 모드 (기본)
            return ResponseEntity.ok(authResponse);
            
        } catch (Exception e) {
            if (redirect) {
                String errorRedirectUrl = (redirectUrl != null ? redirectUrl : "http://localhost:3000/login") 
                        + "?error=" + e.getMessage();
                return new RedirectView(errorRedirectUrl);
            }
            throw new RuntimeException("Failed to process Kakao OAuth: " + e.getMessage());
        }
    }
}

