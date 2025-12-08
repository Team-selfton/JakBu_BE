package com.jakbu.dto;

import com.jakbu.domain.enums.IntervalType;
import jakarta.validation.constraints.NotNull;

public record NotificationSettingRequest(
        @NotNull(message = "Interval type is required")
        IntervalType intervalType,
        
        @NotNull(message = "Enabled status is required")
        Boolean enabled
) {
}

