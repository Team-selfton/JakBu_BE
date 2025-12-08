package com.jakbu.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.jakbu.domain.NotificationSetting;
import com.jakbu.domain.User;
import com.jakbu.domain.enums.IntervalType;
import com.jakbu.dto.FcmTokenRequest;
import com.jakbu.dto.NotificationSettingRequest;
import com.jakbu.dto.NotificationSettingResponse;
import com.jakbu.repository.NotificationSettingRepository;
import com.jakbu.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final FirebaseMessaging firebaseMessaging;

    public NotificationService(UserRepository userRepository, 
                              NotificationSettingRepository notificationSettingRepository,
                              FirebaseMessaging firebaseMessaging) {
        this.userRepository = userRepository;
        this.notificationSettingRepository = notificationSettingRepository;
        this.firebaseMessaging = firebaseMessaging;
    }

    public void saveFcmToken(Long userId, FcmTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.updateFcmToken(request.fcmToken());
        userRepository.save(user);
    }

    public NotificationSettingResponse saveNotificationSetting(Long userId, NotificationSettingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<NotificationSetting> existingSetting = notificationSettingRepository.findByUserId(userId);
        
        NotificationSetting setting;
        if (existingSetting.isPresent()) {
            setting = existingSetting.get();
            setting.updateSetting(request.intervalType(), request.enabled());
        } else {
            setting = new NotificationSetting(user, request.intervalType(), request.enabled());
        }
        
        setting = notificationSettingRepository.save(setting);
        return new NotificationSettingResponse(setting.getId(), setting.getIntervalType(), setting.getEnabled());
    }

    @Transactional(readOnly = true)
    public NotificationSettingResponse getNotificationSetting(Long userId) {
        NotificationSetting setting = notificationSettingRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Notification setting not found"));

        return new NotificationSettingResponse(setting.getId(), setting.getIntervalType(), setting.getEnabled());
    }

    public void sendPushNotification(String fcmToken, String title, String body) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException("Failed to send push notification: " + e.getMessage());
        }
    }
}

