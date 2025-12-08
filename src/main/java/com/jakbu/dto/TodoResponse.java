package com.jakbu.dto;

import com.jakbu.domain.enums.TodoStatus;

import java.time.LocalDate;

public record TodoResponse(
        Long id,
        String title,
        LocalDate date,
        TodoStatus status
) {
}

