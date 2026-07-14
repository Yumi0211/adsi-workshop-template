package com.example.attendance.employee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByParentIdIsNullAndActiveTrue();

    List<Department> findByParentIdAndActiveTrue(Long parentId);

    List<Department> findByActiveTrue();
}
