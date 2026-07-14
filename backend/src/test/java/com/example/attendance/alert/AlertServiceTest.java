package com.example.attendance.alert;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.attendance.common.enums.AlertType;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private AlertServiceImpl service;

    private static final Long EMPLOYEE_ID = 1L;
    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    @BeforeEach
    void setUp() {
        service = new AlertServiceImpl(alertRepository, employeeRepository);
    }

    @Test
    @DisplayName("自分のアラート一覧を取得できる")
    void getMyAlerts_returnsAlerts() {
        var alert = Alert.builder()
                .id(1L)
                .employeeId(EMPLOYEE_ID)
                .type(AlertType.OVERTIME_MONTHLY)
                .message("月間残業時間が45時間を超過しました")
                .createdAt(OffsetDateTime.now(JST))
                .acknowledged(false)
                .build();

        when(alertRepository.findByEmployeeIdOrderByCreatedAtDesc(EMPLOYEE_ID))
                .thenReturn(List.of(alert));
        when(employeeRepository.findAllById(anyList()))
                .thenReturn(List.of(Employee.builder().id(EMPLOYEE_ID).name("田中太郎").build()));

        var result = service.getMyAlerts(EMPLOYEE_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo("OVERTIME_MONTHLY");
        assertThat(result.get(0).employeeName()).isEqualTo("田中太郎");
    }

    @Test
    @DisplayName("全アラート一覧をページネーション付きで取得できる")
    void getAllAlerts_returnsPagedAlerts() {
        var alert = Alert.builder()
                .id(1L)
                .employeeId(EMPLOYEE_ID)
                .type(AlertType.INTERVAL_VIOLATION)
                .message("勤務間インターバルが11時間未満です")
                .createdAt(OffsetDateTime.now(JST))
                .acknowledged(false)
                .build();

        var page = new PageImpl<>(List.of(alert), PageRequest.of(0, 20), 1);
        when(alertRepository.findAllByOrderByCreatedAtDesc(any())).thenReturn(page);
        when(employeeRepository.findAllById(anyList()))
                .thenReturn(List.of(Employee.builder().id(EMPLOYEE_ID).name("田中太郎").build()));

        var result = service.getAllAlerts(0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.page()).isEqualTo(0);
    }

    @Test
    @DisplayName("アラートを確認済みにできる")
    void acknowledge_updatesAlert() {
        var alert = Alert.builder()
                .id(1L)
                .employeeId(EMPLOYEE_ID)
                .type(AlertType.OVERTIME_MONTHLY)
                .message("月間残業時間が45時間を超過しました")
                .createdAt(OffsetDateTime.now(JST))
                .acknowledged(false)
                .build();

        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);
        when(employeeRepository.findAllById(anyList()))
                .thenReturn(List.of(Employee.builder().id(EMPLOYEE_ID).name("田中太郎").build()));

        var result = service.acknowledge(1L);

        assertThat(result.acknowledged()).isTrue();
    }

    @Test
    @DisplayName("存在しないアラートIDで確認済みにすると例外")
    void acknowledge_notFound_throwsException() {
        when(alertRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.acknowledge(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
