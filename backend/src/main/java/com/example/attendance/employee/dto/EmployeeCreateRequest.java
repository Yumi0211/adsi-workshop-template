package com.example.attendance.employee.dto;

import java.time.LocalDate;
import java.util.List;

import com.example.attendance.common.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeCreateRequest(
        @NotBlank @Size(max = 20) String employeeCode,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull Role role,
        @NotNull LocalDate hireDate,
        @NotEmpty List<DepartmentAssignment> departments
) {
    public record DepartmentAssignment(
            @NotNull Long departmentId,
            boolean isPrimary
    ) {}
}
