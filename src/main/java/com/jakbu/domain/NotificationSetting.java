package com.jakbu.domain;

import com.jakbu.domain.enums.IntervalType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_settings")
@Getter
@NoArgsConstructor
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntervalType intervalType;

    @Column(nullable = false)
    private Boolean enabled;

    public NotificationSetting(User user, IntervalType intervalType, Boolean enabled) {
        this.user = user;
        this.intervalType = intervalType;
        this.enabled = enabled;
    }

    public void updateSetting(IntervalType intervalType, Boolean enabled) {
        this.intervalType = intervalType;
        this.enabled = enabled;
    }
}

