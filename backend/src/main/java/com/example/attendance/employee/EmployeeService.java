package com.example.attendance.employee;

import java.util.List;

import com.example.attendance.employee.dto.EmployeeCreateRequest;
import com.example.attendance.employee.dto.EmployeeResponse;
import com.example.attendance.employee.dto.EmployeeUpdateRequest;

public interface EmployeeService {

    EmployeeResponse create(EmployeeCreateRequest request);

    EmployeeResponse findById(Long id);

    Long findIdByEmail(String email);

    List<EmployeeResponse> findAll();

    EmployeeResponse update(Long id, EmployeeUpdateRequest request);

    void deactivate(Long id);
}
