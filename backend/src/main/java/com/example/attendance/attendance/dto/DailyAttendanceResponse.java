package com.example.attendance.attendance.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DailyAttendanceResponse(
        LocalDate date,
        OffsetDateTime clockIn,
        OffsetDateTime clockOut,
        int breakMinutes,
        int workingMinutes,
        int overtimeMinutes,
        int nightMinutes,
        boolean isHolidayWork,
        String status
) {}
