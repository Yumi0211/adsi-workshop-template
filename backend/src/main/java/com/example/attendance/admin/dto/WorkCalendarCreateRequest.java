package com.example.attendance.admin.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WorkCalendarCreateRequest(
        @NotNull LocalDate calendarDate,
        @NotNull @Pattern(regexp = "WORKDAY|HOLIDAY|COMPANY_HOLIDAY") String dayType,
        @Size(max = 100) String description,
        @NotNull @Min(1900) @Max(2100) Integer fiscalYear
) {}
