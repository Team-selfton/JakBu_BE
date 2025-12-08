package com.jakbu.service;

import com.jakbu.domain.User;
import com.jakbu.util.JwtUtil;
import com.jakbu.dto.AuthRequest;
import com.jakbu.dto.AuthResponse;
import com.jakbu.dto.LoginRequest;
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

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse signup(AuthRequest request) {
        if (userRepository.findByAccountId(request.accountId()).isPresent()) {
            throw new RuntimeException("Account ID already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.accountId(), encodedPassword, request.name());
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByAccountId(request.accountId())
                .orElseThrow(() -> new RuntimeException("Invalid account ID or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid account ID or password");
        }

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getName());
    }
}

