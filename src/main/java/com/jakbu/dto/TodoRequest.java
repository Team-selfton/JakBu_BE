package com.jakbu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TodoRequest(
        @NotBlank(message = "Title is required")
        String title,
        
        @NotNull(message = "Date is required")
        LocalDate date
) {
}

