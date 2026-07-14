package com.example.attendance.employee;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.dto.EmployeeCreateRequest;
import com.example.attendance.employee.dto.EmployeeCreateRequest.DepartmentAssignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private EmployeeDepartmentRepository employeeDepartmentRepository;

    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmployeeServiceImpl(employeeRepository, departmentRepository, employeeDepartmentRepository);
    }

    @Test
    @DisplayName("正常な登録リクエストで社員が作成される")
    void create_validRequest_createsEmployee() {
        var request = new EmployeeCreateRequest(
                "EMP001", "田中太郎", "tanaka@example.com", Role.EMPLOYEE,
                LocalDate.of(2024, 4, 1),
                List.of(new DepartmentAssignment(1L, true))
        );

        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("tanaka@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            var emp = inv.getArgument(0, Employee.class);
            emp.setId(1L);
            return emp;
        });
        when(employeeDepartmentRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(
                Department.builder().id(1L).name("営業課").build()));

        var result = service.create(request);

        assertThat(result.employeeCode()).isEqualTo("EMP001");
        assertThat(result.name()).isEqualTo("田中太郎");
        assertThat(result.departments()).hasSize(1);
        assertThat(result.departments().get(0).isPrimary()).isTrue();
    }

    @Test
    @DisplayName("社員番号が重複すると例外が投げられる")
    void create_duplicateCode_throwsBusinessException() {
        var request = new EmployeeCreateRequest(
                "EMP001", "田中太郎", "tanaka@example.com", Role.EMPLOYEE,
                LocalDate.of(2024, 4, 1),
                List.of(new DepartmentAssignment(1L, true))
        );

        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("社員番号");
    }

    @Test
    @DisplayName("メールアドレスが重複すると例外が投げられる")
    void create_duplicateEmail_throwsBusinessException() {
        var request = new EmployeeCreateRequest(
                "EMP001", "田中太郎", "tanaka@example.com", Role.EMPLOYEE,
                LocalDate.of(2024, 4, 1),
                List.of(new DepartmentAssignment(1L, true))
        );

        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("tanaka@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("メールアドレス");
    }

    @Test
    @DisplayName("主所属が指定されていないと例外が投げられる")
    void create_noPrimaryDepartment_throwsBusinessException() {
        var request = new EmployeeCreateRequest(
                "EMP001", "田中太郎", "tanaka@example.com", Role.EMPLOYEE,
                LocalDate.of(2024, 4, 1),
                List.of(new DepartmentAssignment(1L, false))
        );

        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("tanaka@example.com")).thenReturn(false);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("主所属");
    }

    @Test
    @DisplayName("存在するIDで社員情報が取得できる")
    void findById_existingId_returnsEmployee() {
        var employee = Employee.builder()
                .id(1L).employeeCode("EMP001").name("田中太郎")
                .email("tanaka@example.com").role(Role.EMPLOYEE)
                .hireDate(LocalDate.of(2024, 4, 1)).active(true).build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeDepartmentRepository.findByEmployeeIdAndEndDateIsNull(1L))
                .thenReturn(List.of(EmployeeDepartment.builder().departmentId(1L).primary(true).build()));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(
                Department.builder().id(1L).name("営業課").build()));

        var result = service.findById(1L);

        assertThat(result.employeeCode()).isEqualTo("EMP001");
    }

    @Test
    @DisplayName("存在しないIDで ResourceNotFoundException が投げられる")
    void findById_nonExistingId_throwsNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("社員を無効化できる")
    void deactivate_existingEmployee_setsActiveFalse() {
        var employee = Employee.builder().id(1L).active(true).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        service.deactivate(1L);

        assertThat(employee.isActive()).isFalse();
    }
}
