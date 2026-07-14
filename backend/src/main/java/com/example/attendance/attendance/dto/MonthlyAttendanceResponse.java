package com.example.attendance.attendance.dto;

import java.util.List;

public record MonthlyAttendanceResponse(
        Long employeeId,
        int year,
        int month,
        List<DailyAttendanceResponse> records,
        MonthlySummaryResponse summary
) {}
