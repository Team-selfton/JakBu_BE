package com.jakbu.dto;

import com.jakbu.domain.enums.IntervalType;

public record NotificationSettingResponse(
        Long id,
        IntervalType intervalType,
        Boolean enabled
) {
}

