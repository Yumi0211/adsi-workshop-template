package com.example.attendance.employee;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);

    @Query("SELECT ed.employeeId FROM EmployeeDepartment ed WHERE ed.departmentId IN " +
           "(SELECT ed2.departmentId FROM EmployeeDepartment ed2 WHERE ed2.employeeId = :employeeId AND ed2.endDate IS NULL) " +
           "AND ed.employeeId <> :employeeId AND ed.endDate IS NULL " +
           "AND EXISTS (SELECT 1 FROM Employee e WHERE e.id = ed.employeeId AND e.role = 'APPROVER' AND e.active = true)")
    Optional<Long> findApproverIdByEmployeeId(@Param("employeeId") Long employeeId);
}
