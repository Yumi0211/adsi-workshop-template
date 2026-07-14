package com.example.attendance.employee;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.dto.EmployeeCreateRequest;
import com.example.attendance.employee.dto.EmployeeResponse;
import com.example.attendance.employee.dto.EmployeeUpdateRequest;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeDepartmentRepository employeeDepartmentRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               EmployeeDepartmentRepository employeeDepartmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.employeeDepartmentRepository = employeeDepartmentRepository;
    }

    @Override
    public EmployeeResponse create(EmployeeCreateRequest request) {
        validateUniqueness(request.employeeCode(), request.email());
        validatePrimaryDepartment(request.departments());

        var employee = Employee.builder()
                .employeeCode(request.employeeCode())
                .name(request.name())
                .email(request.email())
                .role(request.role())
                .hireDate(request.hireDate())
                .build();

        employee = employeeRepository.save(employee);

        var assignments = saveAssignments(employee.getId(), request.departments());
        final Long empId = employee.getId();
        return EmployeeResponse.from(employee, assignments, deptId -> getDepartmentName(deptId));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        var employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        var assignments = employeeDepartmentRepository.findByEmployeeIdAndEndDateIsNull(id);
        return EmployeeResponse.from(employee, assignments, this::getDepartmentName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream()
                .map(emp -> {
                    var assignments = employeeDepartmentRepository.findByEmployeeIdAndEndDateIsNull(emp.getId());
                    return EmployeeResponse.from(emp, assignments, this::getDepartmentName);
                })
                .toList();
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {
        var employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        if (!employee.getEmail().equals(request.email()) && employeeRepository.existsByEmail(request.email())) {
            throw new BusinessException("このメールアドレスは既に使用されています");
        }

        validatePrimaryDepartment(request.departments());

        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setRole(request.role());
        employee.setVersion(request.version());
        employee = employeeRepository.save(employee);

        var currentAssignments = employeeDepartmentRepository.findByEmployeeIdAndEndDateIsNull(id);
        currentAssignments.forEach(a -> a.setEndDate(LocalDate.now()));
        employeeDepartmentRepository.saveAll(currentAssignments);

        var newAssignments = saveAssignments(id, request.departments());
        return EmployeeResponse.from(employee, newAssignments, this::getDepartmentName);
    }

    @Override
    public void deactivate(Long id) {
        var employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    private void validateUniqueness(String employeeCode, String email) {
        if (employeeRepository.existsByEmployeeCode(employeeCode)) {
            throw new BusinessException("この社員番号は既に使用されています");
        }
        if (employeeRepository.existsByEmail(email)) {
            throw new BusinessException("このメールアドレスは既に使用されています");
        }
    }

    private void validatePrimaryDepartment(List<EmployeeCreateRequest.DepartmentAssignment> departments) {
        long primaryCount = departments.stream().filter(EmployeeCreateRequest.DepartmentAssignment::isPrimary).count();
        if (primaryCount != 1) {
            throw new BusinessException("主所属は1つだけ指定してください");
        }
    }

    private List<EmployeeDepartment> saveAssignments(Long employeeId,
                                                      List<EmployeeCreateRequest.DepartmentAssignment> departments) {
        var assignments = departments.stream()
                .map(d -> EmployeeDepartment.builder()
                        .employeeId(employeeId)
                        .departmentId(d.departmentId())
                        .primary(d.isPrimary())
                        .startDate(LocalDate.now())
                        .build())
                .toList();
        return employeeDepartmentRepository.saveAll(assignments);
    }

    private String getDepartmentName(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .map(Department::getName)
                .orElse("不明");
    }
}
