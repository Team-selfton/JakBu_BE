package com.jakbu.controller;

import com.jakbu.dto.FcmTokenRequest;
import com.jakbu.dto.NotificationSettingRequest;
import com.jakbu.dto.NotificationSettingResponse;
import com.jakbu.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/token")
    public ResponseEntity<Void> saveFcmToken(
            @Valid @RequestBody FcmTokenRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.saveFcmToken(userId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/setting")
    public ResponseEntity<NotificationSettingResponse> saveNotificationSetting(
            @Valid @RequestBody NotificationSettingRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        NotificationSettingResponse response = notificationService.saveNotificationSetting(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/setting")
    public ResponseEntity<NotificationSettingResponse> getNotificationSetting(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        NotificationSettingResponse response = notificationService.getNotificationSetting(userId);
        return ResponseEntity.ok(response);
    }
}

