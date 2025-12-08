package com.jakbu.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailyTodoResetScheduler {

    private final TodoService todoService;

    public DailyTodoResetScheduler(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 매일 자정에 이전 날짜의 DONE 상태를 TODO로 초기화합니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDoneStatusAtMidnight() {
        LocalDate today = LocalDate.now();
        todoService.resetDoneStatusBefore(today);
    }
}

