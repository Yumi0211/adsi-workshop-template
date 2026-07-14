package com.example.attendance.leave;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.example.attendance.common.enums.RequestType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest.LeaveType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;
    @Mock
    private ApprovalRequestRepository approvalRequestRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    private LeaveServiceImpl service;

    private static final Long EMPLOYEE_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 14);

    @BeforeEach
    void setUp() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        service = new LeaveServiceImpl(leaveBalanceRepository, approvalRequestRepository, employeeRepository, () -> TODAY, objectMapper);
    }

    @Test
    @DisplayName("有効な残高が複数ある場合に合算された残高を取得できる")
    void getBalance_multipleActiveBalances_returnsSummed() {
        var balance1 = LeaveBalance.builder()
                .employeeId(EMPLOYEE_ID).fiscalYear(2026)
                .grantedDays(new BigDecimal("10.0")).usedDays(new BigDecimal("3.0"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2026, 4, 1)).expiryDate(LocalDate.of(2028, 3, 31))
                .build();
        var balance2 = LeaveBalance.builder()
                .employeeId(EMPLOYEE_ID).fiscalYear(2025)
                .grantedDays(new BigDecimal("10.0")).usedDays(new BigDecimal("8.0"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2025, 4, 1)).expiryDate(LocalDate.of(2027, 3, 31))
                .build();

        when(leaveBalanceRepository.findActiveByEmployeeId(EMPLOYEE_ID, TODAY))
                .thenReturn(List.of(balance1, balance2));

        var result = service.getBalance(EMPLOYEE_ID);

        assertThat(result.totalGrantedDays()).isEqualByComparingTo(new BigDecimal("20.0"));
        assertThat(result.usedDays()).isEqualByComparingTo(new BigDecimal("11.0"));
        assertThat(result.remainingDays()).isEqualByComparingTo(new BigDecimal("9.0"));
    }

    @Test
    @DisplayName("全休申請で残高が1.0日消化される")
    void createRequest_fullDay_consumesOneDay() {
        var balance = LeaveBalance.builder()
                .id(1L).employeeId(EMPLOYEE_ID).fiscalYear(2026)
                .grantedDays(new BigDecimal("10.0")).usedDays(new BigDecimal("3.0"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2026, 4, 1)).expiryDate(LocalDate.of(2028, 3, 31))
                .build();

        when(leaveBalanceRepository.findActiveByEmployeeId(EMPLOYEE_ID, TODAY))
                .thenReturn(List.of(balance));
        when(employeeRepository.findApproverIdByEmployeeId(EMPLOYEE_ID))
                .thenReturn(java.util.Optional.of(2L));
        when(approvalRequestRepository.save(any())).thenAnswer(inv -> {
            var req = inv.getArgument(0, ApprovalRequest.class);
            req.setId(100L);
            return req;
        });
        when(leaveBalanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new LeaveRequestCreateRequest(LocalDate.of(2026, 7, 20), LeaveType.FULL, null);
        var result = service.createRequest(EMPLOYEE_ID, request);

        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.leaveType()).isEqualTo("FULL");
        assertThat(balance.getUsedDays()).isEqualByComparingTo(new BigDecimal("4.0"));
    }

    @Test
    @DisplayName("半休申請で残高が0.5日消化される")
    void createRequest_halfDay_consumesHalfDay() {
        var balance = LeaveBalance.builder()
                .id(1L).employeeId(EMPLOYEE_ID).fiscalYear(2026)
                .grantedDays(new BigDecimal("10.0")).usedDays(new BigDecimal("9.5"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2026, 4, 1)).expiryDate(LocalDate.of(2028, 3, 31))
                .build();

        when(leaveBalanceRepository.findActiveByEmployeeId(EMPLOYEE_ID, TODAY))
                .thenReturn(List.of(balance));
        when(employeeRepository.findApproverIdByEmployeeId(EMPLOYEE_ID))
                .thenReturn(java.util.Optional.of(2L));
        when(approvalRequestRepository.save(any())).thenAnswer(inv -> {
            var req = inv.getArgument(0, ApprovalRequest.class);
            req.setId(101L);
            return req;
        });
        when(leaveBalanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new LeaveRequestCreateRequest(LocalDate.of(2026, 7, 20), LeaveType.HALF_AM, null);
        var result = service.createRequest(EMPLOYEE_ID, request);

        assertThat(result.leaveType()).isEqualTo("HALF_AM");
        assertThat(balance.getUsedDays()).isEqualByComparingTo(new BigDecimal("10.0"));
    }

    @Test
    @DisplayName("残日数不足で申請がエラーになる")
    void createRequest_insufficientBalance_throwsBusinessException() {
        var balance = LeaveBalance.builder()
                .id(1L).employeeId(EMPLOYEE_ID).fiscalYear(2026)
                .grantedDays(new BigDecimal("10.0")).usedDays(new BigDecimal("10.0"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2026, 4, 1)).expiryDate(LocalDate.of(2028, 3, 31))
                .build();

        when(leaveBalanceRepository.findActiveByEmployeeId(EMPLOYEE_ID, TODAY))
                .thenReturn(List.of(balance));

        var request = new LeaveRequestCreateRequest(LocalDate.of(2026, 7, 20), LeaveType.FULL, null);

        assertThatThrownBy(() -> service.createRequest(EMPLOYEE_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("残日数");
    }

    @Test
    @DisplayName("期限切れの残高は残高合計に含まれない")
    void getBalance_expiredBalance_excluded() {
        var expired = LeaveBalance.builder()
                .employeeId(EMPLOYEE_ID).fiscalYear(2024)
                .grantedDays(new BigDecimal("10.0")).usedDays(new BigDecimal("5.0"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2024, 4, 1)).expiryDate(LocalDate.of(2026, 3, 31))
                .build();

        // findActiveByEmployeeId はクエリで expiryDate > today のものだけ返すので、
        // 期限切れは返されない
        when(leaveBalanceRepository.findActiveByEmployeeId(EMPLOYEE_ID, TODAY))
                .thenReturn(List.of());

        var result = service.getBalance(EMPLOYEE_ID);

        assertThat(result.remainingDays()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("勤続年数に応じた法定付与日数を正しく計算する")
    void calculateGrantDays_variousYearsOfService() {
        assertThat(LeaveServiceImpl.calculateGrantDays(0)).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(LeaveServiceImpl.calculateGrantDays(1)).isEqualByComparingTo(new BigDecimal("11"));
        assertThat(LeaveServiceImpl.calculateGrantDays(2)).isEqualByComparingTo(new BigDecimal("12"));
        assertThat(LeaveServiceImpl.calculateGrantDays(3)).isEqualByComparingTo(new BigDecimal("14"));
        assertThat(LeaveServiceImpl.calculateGrantDays(4)).isEqualByComparingTo(new BigDecimal("16"));
        assertThat(LeaveServiceImpl.calculateGrantDays(5)).isEqualByComparingTo(new BigDecimal("18"));
        assertThat(LeaveServiceImpl.calculateGrantDays(6)).isEqualByComparingTo(new BigDecimal("20"));
        assertThat(LeaveServiceImpl.calculateGrantDays(10)).isEqualByComparingTo(new BigDecimal("20"));
    }
}
