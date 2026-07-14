package com.example.attendance.attendance.dto;

public record MonthlySummaryResponse(
        Long employeeId,
        int year,
        int month,
        int totalWorkingMinutes,
        int totalOvertimeMinutes,
        int totalNightMinutes,
        int totalHolidayWorkMinutes,
        int workingDays
) {}
