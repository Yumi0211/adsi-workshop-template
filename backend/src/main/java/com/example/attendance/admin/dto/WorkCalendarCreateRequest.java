package com.example.attendance.admin.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkCalendarCreateRequest(
        @NotNull LocalDate calendarDate,
        @NotNull String dayType,
        @Size(max = 100) String description,
        @NotNull Integer fiscalYear
) {}
