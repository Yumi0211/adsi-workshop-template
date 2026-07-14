package com.example.attendance.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkCalendarUpdateRequest(
        @NotNull String dayType,
        @Size(max = 100) String description
) {}
