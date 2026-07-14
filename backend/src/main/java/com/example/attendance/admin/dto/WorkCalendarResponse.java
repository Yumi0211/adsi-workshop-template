package com.example.attendance.admin.dto;

import java.time.LocalDate;

public record WorkCalendarResponse(
        Long id,
        LocalDate calendarDate,
        String dayType,
        String description,
        int fiscalYear
) {}
