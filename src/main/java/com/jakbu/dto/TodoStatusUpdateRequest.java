package com.jakbu.dto;

import jakarta.validation.constraints.NotNull;

public record TodoStatusUpdateRequest(
        @NotNull(message = "done is required")
        Boolean done
) {
}

