package com.example.attendance.employee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeDepartmentRepository extends JpaRepository<EmployeeDepartment, Long> {

    List<EmployeeDepartment> findByEmployeeIdAndEndDateIsNull(Long employeeId);

    List<EmployeeDepartment> findByEmployeeIdInAndEndDateIsNull(List<Long> employeeIds);

    List<EmployeeDepartment> findByDepartmentIdAndEndDateIsNull(Long departmentId);

    boolean existsByDepartmentIdAndEndDateIsNull(Long departmentId);
}
