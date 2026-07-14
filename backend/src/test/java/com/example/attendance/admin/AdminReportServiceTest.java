package com.example.attendance.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.attendance.DailyAttendance;
import com.example.attendance.attendance.DailyAttendanceRepository;
import com.example.attendance.common.enums.AttendanceStatus;
import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeDepartment;
import com.example.attendance.employee.EmployeeDepartmentRepository;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.employee.Department;
import com.example.attendance.employee.DepartmentRepository;
import com.example.attendance.leave.LeaveBalance;
import com.example.attendance.leave.LeaveBalanceRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock
    private DailyAttendanceRepository dailyAttendanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeDepartmentRepository employeeDepartmentRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    private AdminReportServiceImpl service;

    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    @BeforeEach
    void setUp() {
        service = new AdminReportServiceImpl(
                dailyAttendanceRepository,
                employeeRepository,
                employeeDepartmentRepository,
                departmentRepository,
                leaveBalanceRepository);
    }

    @Test
    @DisplayName("残業レポート: 月間残業と年間残業が集計される")
    void getOvertimeReport_calculatesMonthlyAndYearly() {
        var employees = List.of(
                buildEmployee(1L, "EMP001", "田中太郎"),
                buildEmployee(2L, "EMP002", "佐藤花子"));
        when(employeeRepository.findAll()).thenReturn(employees);

        var monthlyAttendances = List.of(
                buildAttendance(1L, LocalDate.of(2026, 7, 1), 60),
                buildAttendance(1L, LocalDate.of(2026, 7, 2), 90),
                buildAttendance(2L, LocalDate.of(2026, 7, 1), 30));
        when(dailyAttendanceRepository.findAllByDateRange(
                eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 7, 31))))
                .thenReturn(monthlyAttendances);

        var yearlyAttendances = List.of(
                buildAttendance(1L, LocalDate.of(2026, 4, 1), 2400),
                buildAttendance(1L, LocalDate.of(2026, 7, 1), 150),
                buildAttendance(2L, LocalDate.of(2026, 5, 1), 100));
        when(dailyAttendanceRepository.findAllByDateRange(
                eq(LocalDate.of(2026, 4, 1)), eq(LocalDate.of(2026, 7, 31))))
                .thenReturn(yearlyAttendances);

        stubDepartmentInfo(1L, "開発部");
        stubDepartmentInfo(2L, "営業部");

        var result = service.getOvertimeReport(2026, 7);

        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isEqualTo(7);
        assertThat(result.records()).hasSize(2);

        var tanaka = result.records().stream()
                .filter(r -> r.employeeId().equals(1L)).findFirst().orElseThrow();
        assertThat(tanaka.overtimeMinutes()).isEqualTo(150);
        assertThat(tanaka.yearlyOvertimeMinutes()).isEqualTo(2550);
        assertThat(tanaka.isOverMonthlyLimit()).isFalse();
        assertThat(tanaka.isOverYearlyLimit()).isFalse();
    }

    @Test
    @DisplayName("残業レポート: 月45h超過でisOverMonthlyLimitがtrue")
    void getOvertimeReport_overMonthlyLimit_flagsTrue() {
        var employees = List.of(buildEmployee(1L, "EMP001", "田中太郎"));
        when(employeeRepository.findAll()).thenReturn(employees);

        var monthlyAttendances = List.of(
                buildAttendance(1L, LocalDate.of(2026, 7, 1), 2701));
        when(dailyAttendanceRepository.findAllByDateRange(
                eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 7, 31))))
                .thenReturn(monthlyAttendances);

        when(dailyAttendanceRepository.findAllByDateRange(
                eq(LocalDate.of(2026, 4, 1)), eq(LocalDate.of(2026, 7, 31))))
                .thenReturn(monthlyAttendances);

        stubDepartmentInfo(1L, "開発部");

        var result = service.getOvertimeReport(2026, 7);

        assertThat(result.records().get(0).isOverMonthlyLimit()).isTrue();
    }

    @Test
    @DisplayName("残業レポート: 年360h超過でisOverYearlyLimitがtrue")
    void getOvertimeReport_overYearlyLimit_flagsTrue() {
        var employees = List.of(buildEmployee(1L, "EMP001", "田中太郎"));
        when(employeeRepository.findAll()).thenReturn(employees);

        var monthlyAttendances = List.of(
                buildAttendance(1L, LocalDate.of(2026, 7, 1), 100));
        when(dailyAttendanceRepository.findAllByDateRange(
                eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 7, 31))))
                .thenReturn(monthlyAttendances);

        var yearlyAttendances = List.of(
                buildAttendance(1L, LocalDate.of(2026, 4, 1), 21601));
        when(dailyAttendanceRepository.findAllByDateRange(
                eq(LocalDate.of(2026, 4, 1)), eq(LocalDate.of(2026, 7, 31))))
                .thenReturn(yearlyAttendances);

        stubDepartmentInfo(1L, "開発部");

        var result = service.getOvertimeReport(2026, 7);

        assertThat(result.records().get(0).isOverYearlyLimit()).isTrue();
    }

    @Test
    @DisplayName("有給5日未取得一覧: 5日未満の社員のみ返す")
    void getLeaveObligationReport_returnsUnderObligationOnly() {
        var employees = List.of(
                buildEmployee(1L, "EMP001", "田中太郎"),
                buildEmployee(2L, "EMP002", "佐藤花子"));
        when(employeeRepository.findAll()).thenReturn(employees);

        var balances = List.of(
                buildLeaveBalance(1L, 2025, new BigDecimal("3.0")),
                buildLeaveBalance(2L, 2025, new BigDecimal("6.0")));
        when(leaveBalanceRepository.findByFiscalYear(2025)).thenReturn(balances);

        stubDepartmentInfo(1L, "開発部");

        var result = service.getLeaveObligationReport(2025);

        assertThat(result.fiscalYear()).isEqualTo(2025);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).employeeId()).isEqualTo(1L);
        assertThat(result.records().get(0).usedDays()).isEqualByComparingTo(new BigDecimal("3.0"));
        assertThat(result.records().get(0).obligationRemaining()).isEqualByComparingTo(new BigDecimal("2.0"));
    }

    @Test
    @DisplayName("有給5日未取得一覧: 全員5日以上取得なら空リスト")
    void getLeaveObligationReport_allMet_returnsEmpty() {
        var employees = List.of(buildEmployee(1L, "EMP001", "田中太郎"));
        when(employeeRepository.findAll()).thenReturn(employees);

        var balances = List.of(
                buildLeaveBalance(1L, 2025, new BigDecimal("5.0")));
        when(leaveBalanceRepository.findByFiscalYear(2025)).thenReturn(balances);

        var result = service.getLeaveObligationReport(2025);

        assertThat(result.fiscalYear()).isEqualTo(2025);
        assertThat(result.records()).isEmpty();
    }

    private Employee buildEmployee(Long id, String code, String name) {
        return Employee.builder()
                .id(id)
                .employeeCode(code)
                .name(name)
                .email(code + "@example.com")
                .role(Role.EMPLOYEE)
                .hireDate(LocalDate.of(2020, 4, 1))
                .active(true)
                .build();
    }

    private DailyAttendance buildAttendance(Long employeeId, LocalDate date, int overtimeMinutes) {
        return DailyAttendance.builder()
                .employeeId(employeeId)
                .attendanceDate(date)
                .clockIn(OffsetDateTime.of(date.atTime(9, 0), JST))
                .clockOut(OffsetDateTime.of(date.atTime(18, 0), JST))
                .breakMinutes(60)
                .workingMinutes(480 + overtimeMinutes)
                .overtimeMinutes(overtimeMinutes)
                .nightMinutes(0)
                .holidayWork(false)
                .status(AttendanceStatus.PRESENT)
                .build();
    }

    private LeaveBalance buildLeaveBalance(Long employeeId, int fiscalYear, BigDecimal usedDays) {
        return LeaveBalance.builder()
                .employeeId(employeeId)
                .fiscalYear(fiscalYear)
                .grantedDays(new BigDecimal("20.0"))
                .usedDays(usedDays)
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(fiscalYear, 4, 1))
                .expiryDate(LocalDate.of(fiscalYear + 2, 3, 31))
                .build();
    }

    private void stubDepartmentInfo(Long employeeId, String deptName) {
        var membership = EmployeeDepartment.builder()
                .employeeId(employeeId)
                .departmentId(employeeId * 10)
                .primary(true)
                .startDate(LocalDate.of(2020, 4, 1))
                .build();
        when(employeeDepartmentRepository.findByEmployeeIdAndEndDateIsNull(employeeId))
                .thenReturn(List.of(membership));

        var dept = Department.builder()
                .id(employeeId * 10)
                .name(deptName)
                .build();
        when(departmentRepository.findById(employeeId * 10))
                .thenReturn(java.util.Optional.of(dept));
    }
}
