package com.example.attendance.alert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.common.enums.AlertType;
import com.example.attendance.leave.LeaveBalance;
import com.example.attendance.leave.LeaveBalanceRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveObligationAlertServiceTest {

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private AlertRepository alertRepository;

    private LeaveObligationAlertServiceImpl service;

    private static final Long EMPLOYEE_ID = 1L;

    @BeforeEach
    void setUp() {
        service = new LeaveObligationAlertServiceImpl(leaveBalanceRepository, alertRepository);
    }

    @Test
    @DisplayName("残り3ヶ月時点で取得2日以下: アラートが生成される")
    void checkLeaveObligation_under3days_createsAlert() {
        var balance = LeaveBalance.builder()
                .employeeId(EMPLOYEE_ID)
                .fiscalYear(2026)
                .grantedDays(new BigDecimal("20.0"))
                .usedDays(new BigDecimal("2.0"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2026, 4, 1))
                .expiryDate(LocalDate.of(2028, 3, 31))
                .build();

        when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(EMPLOYEE_ID, 2026))
                .thenReturn(List.of(balance));
        when(alertRepository.existsByEmployeeIdAndTypeAndCreatedAtAfter(
                eq(EMPLOYEE_ID), eq(AlertType.LEAVE_OBLIGATION), any(OffsetDateTime.class)))
                .thenReturn(false);

        service.checkLeaveObligation(EMPLOYEE_ID, 2026);

        var captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(captor.capture());
        var alert = captor.getValue();
        assertThat(alert.getType()).isEqualTo(AlertType.LEAVE_OBLIGATION);
        assertThat(alert.getMessage()).contains("5日");
    }

    @Test
    @DisplayName("取得3日以上: アラートが生成されない")
    void checkLeaveObligation_3daysOrMore_noAlert() {
        var balance = LeaveBalance.builder()
                .employeeId(EMPLOYEE_ID)
                .fiscalYear(2026)
                .grantedDays(new BigDecimal("20.0"))
                .usedDays(new BigDecimal("5.0"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2026, 4, 1))
                .expiryDate(LocalDate.of(2028, 3, 31))
                .build();

        when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(EMPLOYEE_ID, 2026))
                .thenReturn(List.of(balance));

        service.checkLeaveObligation(EMPLOYEE_ID, 2026);

        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("残高データがない場合: アラートなし")
    void checkLeaveObligation_noBalance_noAlert() {
        when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(EMPLOYEE_ID, 2026))
                .thenReturn(List.of());

        service.checkLeaveObligation(EMPLOYEE_ID, 2026);

        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("重複アラートは生成されない")
    void checkLeaveObligation_duplicate_notCreated() {
        var balance = LeaveBalance.builder()
                .employeeId(EMPLOYEE_ID)
                .fiscalYear(2026)
                .grantedDays(new BigDecimal("20.0"))
                .usedDays(new BigDecimal("1.0"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2026, 4, 1))
                .expiryDate(LocalDate.of(2028, 3, 31))
                .build();

        when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(EMPLOYEE_ID, 2026))
                .thenReturn(List.of(balance));
        when(alertRepository.existsByEmployeeIdAndTypeAndCreatedAtAfter(
                eq(EMPLOYEE_ID), eq(AlertType.LEAVE_OBLIGATION), any(OffsetDateTime.class)))
                .thenReturn(true);

        service.checkLeaveObligation(EMPLOYEE_ID, 2026);

        verify(alertRepository, never()).save(any());
    }
}
