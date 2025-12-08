package com.jakbu.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true, unique = true)
    private Long kakaoId;

    @Column(nullable = true)
    private String fcmToken;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    /**
     * 카카오 소셜 로그인 사용자 생성 팩토리 메서드
     * 
     * @param kakaoId 카카오 사용자 ID
     * @param name 사용자 이름 (닉네임)
     * @param email 이메일 (nullable)
     * @return User 엔티티
     */
    public static User createKakaoUser(Long kakaoId, String name, String email) {
        User user = new User();
        user.kakaoId = kakaoId;
        user.name = name;
        user.email = email;
        return user;
    }

    /**
     * 기존 사용자 계정에 카카오 계정 연결
     * 
     * @param user 기존 사용자
     * @param kakaoId 카카오 사용자 ID
     * @return 카카오 계정이 연결된 User 엔티티
     */
    public static User linkKakaoAccount(User user, Long kakaoId) {
        if (user.kakaoId == null) {
            user.kakaoId = kakaoId;
        }
        return user;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}

