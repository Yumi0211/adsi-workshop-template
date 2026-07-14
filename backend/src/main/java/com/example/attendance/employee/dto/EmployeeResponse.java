package com.example.attendance.employee.dto;

import java.time.LocalDate;
import java.util.List;

import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeDepartment;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String name,
        String email,
        Role role,
        LocalDate hireDate,
        boolean active,
        List<DepartmentInfo> departments,
        Long version
) {
    public record DepartmentInfo(
            Long departmentId,
            String departmentName,
            boolean isPrimary
    ) {}

    public static EmployeeResponse from(Employee employee, List<EmployeeDepartment> assignments,
                                         java.util.function.Function<Long, String> deptNameResolver) {
        var depts = assignments.stream()
                .map(a -> new DepartmentInfo(a.getDepartmentId(), deptNameResolver.apply(a.getDepartmentId()), a.isPrimary()))
                .toList();
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getName(),
                employee.getEmail(),
                employee.getRole(),
                employee.getHireDate(),
                employee.isActive(),
                depts,
                employee.getVersion()
        );
    }
}
