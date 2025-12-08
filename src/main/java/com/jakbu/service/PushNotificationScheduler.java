package com.jakbu.service;

import com.jakbu.domain.NotificationSetting;
import com.jakbu.domain.enums.IntervalType;
import com.jakbu.repository.NotificationSettingRepository;
import com.jakbu.repository.TodoRepository;
import com.jakbu.domain.enums.TodoStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PushNotificationScheduler {

    private final NotificationSettingRepository notificationSettingRepository;
    private final TodoRepository todoRepository;
    private final NotificationService notificationService;

    public PushNotificationScheduler(NotificationSettingRepository notificationSettingRepository,
                                    TodoRepository todoRepository,
                                    NotificationService notificationService) {
        this.notificationSettingRepository = notificationSettingRepository;
        this.todoRepository = todoRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 7200000) // 2시간마다 실행 (밀리초 단위)
    @Transactional(readOnly = true)
    public void sendTwoHourNotifications() {
        List<NotificationSetting> settings = notificationSettingRepository.findByEnabledTrue();
        LocalDate today = LocalDate.now();

        for (NotificationSetting setting : settings) {
            if (setting.getIntervalType() == IntervalType.TWO_HOUR && setting.getEnabled()) {
                Long userId = setting.getUser().getId();
                
                // 미완료 TODO가 있는지 확인
                boolean hasIncompleteTodos = todoRepository.existsByUserIdAndDateAndStatus(
                        userId, today, TodoStatus.TODO);

                if (hasIncompleteTodos && setting.getUser().getFcmToken() != null) {
                    notificationService.sendPushNotification(
                            setting.getUser().getFcmToken(),
                            "JakBu 알림",
                            "아직 완료하지 않은 할 일이 있어요!"
                    );
                }
            }
        }
    }

    @Scheduled(fixedRate = 14400000) // 4시간마다 실행 (밀리초 단위)
    @Transactional(readOnly = true)
    public void sendFourHourNotifications() {
        List<NotificationSetting> settings = notificationSettingRepository.findByEnabledTrue();
        LocalDate today = LocalDate.now();

        for (NotificationSetting setting : settings) {
            if (setting.getIntervalType() == IntervalType.FOUR_HOUR && setting.getEnabled()) {
                Long userId = setting.getUser().getId();
                
                // 미완료 TODO가 있는지 확인
                boolean hasIncompleteTodos = todoRepository.existsByUserIdAndDateAndStatus(
                        userId, today, TodoStatus.TODO);

                if (hasIncompleteTodos && setting.getUser().getFcmToken() != null) {
                    notificationService.sendPushNotification(
                            setting.getUser().getFcmToken(),
                            "JakBu 알림",
                            "아직 완료하지 않은 할 일이 있어요!"
                    );
                }
            }
        }
    }

    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시에 실행
    @Transactional(readOnly = true)
    public void sendDailyNotifications() {
        List<NotificationSetting> settings = notificationSettingRepository.findByEnabledTrue();
        LocalDate today = LocalDate.now();

        for (NotificationSetting setting : settings) {
            if (setting.getIntervalType() == IntervalType.DAILY && setting.getEnabled()) {
                Long userId = setting.getUser().getId();
                
                // 미완료 TODO가 있는지 확인
                boolean hasIncompleteTodos = todoRepository.existsByUserIdAndDateAndStatus(
                        userId, today, TodoStatus.TODO);

                if (hasIncompleteTodos && setting.getUser().getFcmToken() != null) {
                    notificationService.sendPushNotification(
                            setting.getUser().getFcmToken(),
                            "JakBu 알림",
                            "오늘의 할 일을 확인해보세요!"
                    );
                }
            }
        }
    }
}

