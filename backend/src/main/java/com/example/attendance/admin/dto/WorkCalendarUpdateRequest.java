package com.example.attendance.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WorkCalendarUpdateRequest(
        @NotNull @Pattern(regexp = "WORKDAY|HOLIDAY|COMPANY_HOLIDAY") String dayType,
        @Size(max = 100) String description
) {}
