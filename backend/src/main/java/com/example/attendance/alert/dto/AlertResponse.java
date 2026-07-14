package com.example.attendance.alert.dto;

import java.time.OffsetDateTime;

public record AlertResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String type,
        String message,
        OffsetDateTime createdAt,
        boolean acknowledged
) {
}
