package com.jakbu.controller;


import com.jakbu.dto.AuthRequest;
import com.jakbu.dto.AuthResponse;
import com.jakbu.dto.KakaoLoginRequest;
import com.jakbu.dto.LoginRequest;
import com.jakbu.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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

    @PostMapping("/kakao")
    public ResponseEntity<AuthResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        AuthResponse response = authService.kakaoLogin(request);
        return ResponseEntity.ok(response);
    }
}

