package com.example.attendance.leave.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record LeaveRequestCreateRequest(
        @NotNull LocalDate leaveDate,
        @NotNull LeaveType leaveType,
        String reason
) {
    public enum LeaveType {
        FULL, HALF_AM, HALF_PM
    }
}
