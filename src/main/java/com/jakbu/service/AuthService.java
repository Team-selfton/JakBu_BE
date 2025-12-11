package com.jakbu.service;

import com.jakbu.domain.User;
import com.jakbu.repository.NotificationSettingRepository;
import com.jakbu.repository.TodoRepository;
import com.jakbu.util.JwtUtil;
import com.jakbu.dto.AuthRequest;
import com.jakbu.dto.AuthResponse;
import com.jakbu.dto.LoginRequest;
import com.jakbu.dto.RefreshTokenRequest;
import com.jakbu.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TodoRepository todoRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       TodoRepository todoRepository,
                       NotificationSettingRepository notificationSettingRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.todoRepository = todoRepository;
        this.notificationSettingRepository = notificationSettingRepository;
    }

    public AuthResponse signup(AuthRequest request) {
        if (userRepository.findByAccountId(request.accountId()).isPresent()) {
            throw new RuntimeException("Account ID already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.accountId(), encodedPassword, request.name());
        user = userRepository.save(user);

        // 엑세스 토큰과 리프레시 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 리프레시 토큰을 DB에 저장
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByAccountId(request.accountId())
                .orElseThrow(() -> new RuntimeException("Invalid account ID or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid account ID or password");
        }

        // 엑세스 토큰과 리프레시 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 리프레시 토큰을 DB에 저장
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getName());
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 엑세스 토큰 발급
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        // 리프레시 토큰이 DB에 있는지 확인
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // 리프레시 토큰 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            // 유효하지 않은 리프레시 토큰이면 DB에서 제거
            user.updateRefreshToken(null);
            userRepository.save(user);
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // 새로운 엑세스 토큰 생성
        String newAccessToken = jwtUtil.generateAccessToken(user.getId());

        return new AuthResponse(newAccessToken, refreshToken, user.getId(), user.getName());
    }

    /**
     * 로그아웃: 저장된 리프레시 토큰을 제거하여 재발급을 막음
     */
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.updateRefreshToken(null);
        userRepository.save(user);
    }

    /**
     * 회원 탈퇴: 연관된 Todo 및 알림 설정 제거 후 사용자 삭제
     */
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        todoRepository.deleteByUserId(userId);
        notificationSettingRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }
}

