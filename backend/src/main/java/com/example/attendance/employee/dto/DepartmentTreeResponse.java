package com.example.attendance.employee.dto;

import java.util.List;

import com.example.attendance.common.enums.DepartmentLevel;

public record DepartmentTreeResponse(
        Long id,
        String name,
        DepartmentLevel level,
        boolean active,
        List<DepartmentTreeResponse> children
) {}
