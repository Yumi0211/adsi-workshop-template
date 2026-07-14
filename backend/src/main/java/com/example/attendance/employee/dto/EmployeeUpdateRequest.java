package com.example.attendance.employee.dto;

import java.util.List;

import com.example.attendance.common.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull Role role,
        @NotEmpty List<EmployeeCreateRequest.DepartmentAssignment> departments,
        @NotNull Long version
) {}
