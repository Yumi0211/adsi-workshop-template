package com.example.attendance.employee;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.common.enums.DepartmentLevel;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.dto.DepartmentCreateRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private EmployeeDepartmentRepository employeeDepartmentRepository;

    private DepartmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DepartmentServiceImpl(departmentRepository, employeeDepartmentRepository);
    }

    @Test
    @DisplayName("正常なリクエストで部門が作成される")
    void create_validRequest_createsDepartment() {
        var request = new DepartmentCreateRequest("営業本部", DepartmentLevel.HEADQUARTERS, null);

        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> {
            var dept = inv.getArgument(0, Department.class);
            dept.setId(1L);
            return dept;
        });

        var result = service.create(request);

        assertThat(result.name()).isEqualTo("営業本部");
        assertThat(result.level()).isEqualTo(DepartmentLevel.HEADQUARTERS);
        assertThat(result.parentId()).isNull();
    }

    @Test
    @DisplayName("親部門を指定して部門が作成される")
    void create_withParent_setsParentRelation() {
        var parent = Department.builder().id(1L).name("営業本部").level(DepartmentLevel.HEADQUARTERS).build();
        var request = new DepartmentCreateRequest("営業部", DepartmentLevel.DIVISION, 1L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> {
            var dept = inv.getArgument(0, Department.class);
            dept.setId(2L);
            return dept;
        });

        var result = service.create(request);

        assertThat(result.name()).isEqualTo("営業部");
        assertThat(result.parentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("部門ツリーが階層構造で取得できる")
    void findTree_returnsHierarchy() {
        var root = Department.builder().id(1L).name("営業本部").level(DepartmentLevel.HEADQUARTERS).active(true).build();
        var child = Department.builder().id(2L).name("営業部").level(DepartmentLevel.DIVISION).parentId(1L).active(true).build();

        when(departmentRepository.findByParentIdIsNullAndActiveTrue()).thenReturn(List.of(root));
        when(departmentRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(child));
        when(departmentRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of());

        var result = service.findTree();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("営業本部");
        assertThat(result.get(0).children()).hasSize(1);
        assertThat(result.get(0).children().get(0).name()).isEqualTo("営業部");
    }

    @Test
    @DisplayName("配下社員がいない部門は無効化できる")
    void deactivate_noEmployees_deactivates() {
        var department = Department.builder().id(1L).active(true).build();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeDepartmentRepository.existsByDepartmentIdAndEndDateIsNull(1L)).thenReturn(false);
        when(departmentRepository.save(any())).thenReturn(department);

        service.deactivate(1L);

        assertThat(department.isActive()).isFalse();
    }

    @Test
    @DisplayName("配下社員がいる部門は無効化できない")
    void deactivate_hasEmployees_throwsBusinessException() {
        var department = Department.builder().id(1L).active(true).build();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeDepartmentRepository.existsByDepartmentIdAndEndDateIsNull(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.deactivate(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("配下に所属社員");
    }

    @Test
    @DisplayName("存在しない親部門を指定すると例外")
    void create_invalidParent_throwsNotFound() {
        var request = new DepartmentCreateRequest("営業部", DepartmentLevel.DIVISION, 999L);
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
