package com.example.attendance.employee.dto;

import com.example.attendance.common.enums.DepartmentLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull DepartmentLevel level,
        Long parentId
) {}
