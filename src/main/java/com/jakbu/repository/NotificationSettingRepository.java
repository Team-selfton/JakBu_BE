package com.jakbu.repository;

import com.jakbu.domain.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    Optional<NotificationSetting> findByUserId(Long userId);
    
    List<NotificationSetting> findByEnabledTrue();
}

