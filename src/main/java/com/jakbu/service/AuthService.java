package com.jakbu.service;

import com.jakbu.domain.User;
import com.jakbu.dto.AuthRequest;
import com.jakbu.dto.AuthResponse;
import com.jakbu.dto.KakaoLoginRequest;
import com.jakbu.dto.LoginRequest;
import com.jakbu.repository.UserRepository;
import com.jakbu.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final WebClient webClient;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.webClient = WebClient.builder()
                .baseUrl("https://kapi.kakao.com")
                .build();
    }

    public AuthResponse signup(AuthRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.email(), encodedPassword, request.name());
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getName());
    }

    public AuthResponse kakaoLogin(KakaoLoginRequest request) {
        // 카카오 API 호출하여 사용자 정보 가져오기
        Map<String, Object> kakaoUserInfo = getKakaoUserInfo(request.accessToken());
        
        Long kakaoId = Long.parseLong(kakaoUserInfo.get("id").toString());
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String name = profile.get("nickname").toString();

        // 카카오 ID로 사용자 조회
        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // 자동 회원가입
            user = User.createKakaoUser(kakaoId, name);
            user = userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getName());
    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri("/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Kakao user info: " + e.getMessage());
        }
    }
}

