package com.example.attendance.approval.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public record TimeCorrectionCreateRequest(
        @NotNull @PastOrPresent LocalDate targetDate,
        OffsetDateTime correctedClockIn,
        OffsetDateTime correctedClockOut,
        String reason
) {
}
