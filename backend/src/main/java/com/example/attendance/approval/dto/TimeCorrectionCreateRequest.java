package com.example.attendance.approval.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

public record TimeCorrectionCreateRequest(
        @NotNull LocalDate targetDate,
        OffsetDateTime correctedClockIn,
        OffsetDateTime correctedClockOut,
        String reason
) {
}
