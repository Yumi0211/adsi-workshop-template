package com.example.attendance.employee;

import java.util.List;

import com.example.attendance.employee.dto.DepartmentCreateRequest;
import com.example.attendance.employee.dto.DepartmentResponse;
import com.example.attendance.employee.dto.DepartmentTreeResponse;
import com.example.attendance.employee.dto.DepartmentUpdateRequest;

public interface DepartmentService {

    DepartmentResponse create(DepartmentCreateRequest request);

    DepartmentResponse update(Long id, DepartmentUpdateRequest request);

    void deactivate(Long id);

    List<DepartmentTreeResponse> findTree();
}
