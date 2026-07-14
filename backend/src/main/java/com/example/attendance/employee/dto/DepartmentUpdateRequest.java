package com.example.attendance.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        Long parentId,
        @NotNull Long version
) {}
