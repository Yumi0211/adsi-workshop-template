package com.example.attendance.employee;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.dto.DepartmentCreateRequest;
import com.example.attendance.employee.dto.DepartmentResponse;
import com.example.attendance.employee.dto.DepartmentTreeResponse;
import com.example.attendance.employee.dto.DepartmentUpdateRequest;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeDepartmentRepository employeeDepartmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                  EmployeeDepartmentRepository employeeDepartmentRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeDepartmentRepository = employeeDepartmentRepository;
    }

    @Override
    public DepartmentResponse create(DepartmentCreateRequest request) {
        if (request.parentId() != null) {
            departmentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.parentId()));
        }

        var department = Department.builder()
                .name(request.name())
                .level(request.level())
                .parentId(request.parentId())
                .build();

        department = departmentRepository.save(department);
        return DepartmentResponse.from(department);
    }

    @Override
    public DepartmentResponse update(Long id, DepartmentUpdateRequest request) {
        var department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        department.setName(request.name());
        department.setParentId(request.parentId());
        department.setVersion(request.version());
        department = departmentRepository.save(department);
        return DepartmentResponse.from(department);
    }

    @Override
    public void deactivate(Long id) {
        var department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        if (employeeDepartmentRepository.existsByDepartmentIdAndEndDateIsNull(id)) {
            throw new BusinessException("配下に所属社員がいるため無効化できません");
        }

        department.setActive(false);
        departmentRepository.save(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentTreeResponse> findTree() {
        var roots = departmentRepository.findByParentIdIsNullAndActiveTrue();
        return roots.stream()
                .map(this::buildTree)
                .toList();
    }

    private DepartmentTreeResponse buildTree(Department department) {
        var children = departmentRepository.findByParentIdAndActiveTrue(department.getId())
                .stream()
                .map(this::buildTree)
                .toList();
        return new DepartmentTreeResponse(
                department.getId(),
                department.getName(),
                department.getLevel(),
                department.isActive(),
                children
        );
    }
}
