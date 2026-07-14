package com.example.attendance.employee.dto;

import com.example.attendance.common.enums.DepartmentLevel;
import com.example.attendance.employee.Department;

public record DepartmentResponse(
        Long id,
        String name,
        DepartmentLevel level,
        Long parentId,
        boolean active,
        Long version
) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getLevel(),
                department.getParentId(),
                department.isActive(),
                department.getVersion()
        );
    }
}
