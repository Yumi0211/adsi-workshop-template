package com.example.attendance.attendance;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.common.enums.AttendanceStatus;
import com.example.attendance.employee.EmployeeDepartment;
import com.example.attendance.employee.EmployeeDepartmentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportServiceTest {

    @Mock
    private DailyAttendanceRepository dailyAttendanceRepository;

    @Mock
    private EmployeeDepartmentRepository employeeDepartmentRepository;

    private MonthlyReportServiceImpl service;

    private static final Long EMPLOYEE_ID = 1L;
    private static final int YEAR = 2026;
    private static final int MONTH = 7;
    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    @BeforeEach
    void setUp() {
        service = new MonthlyReportServiceImpl(dailyAttendanceRepository, employeeDepartmentRepository);
    }

    @Test
    @DisplayName("月次勤怠一覧を取得できる")
    void getMonthlyAttendance_returnsRecords() {
        var attendances = List.of(
                buildAttendance(LocalDate.of(2026, 7, 1), 450, 0, 0, false),
                buildAttendance(LocalDate.of(2026, 7, 2), 540, 90, 0, false));
        stubAttendances(attendances);

        var result = service.getMonthlyAttendance(EMPLOYEE_ID, YEAR, MONTH);

        assertThat(result.employeeId()).isEqualTo(EMPLOYEE_ID);
        assertThat(result.year()).isEqualTo(YEAR);
        assertThat(result.month()).isEqualTo(MONTH);
        assertThat(result.records()).hasSize(2);
    }

    @Test
    @DisplayName("月次サマリーで合計勤務・残業・深夜・休日・勤務日数が計算される")
    void getMonthlySummary_calculatesCorrectly() {
        var attendances = List.of(
                buildAttendance(LocalDate.of(2026, 7, 1), 450, 0, 0, false),
                buildAttendance(LocalDate.of(2026, 7, 2), 540, 90, 60, false),
                buildAttendance(LocalDate.of(2026, 7, 5), 480, 30, 0, true));
        stubAttendances(attendances);

        var result = service.getMonthlySummary(EMPLOYEE_ID, YEAR, MONTH);

        assertThat(result.totalWorkingMinutes()).isEqualTo(1470);
        assertThat(result.totalOvertimeMinutes()).isEqualTo(120);
        assertThat(result.totalNightMinutes()).isEqualTo(60);
        assertThat(result.totalHolidayWorkMinutes()).isEqualTo(480);
        assertThat(result.workingDays()).isEqualTo(3);
    }

    @Test
    @DisplayName("勤怠データなしの月は空のレコードとサマリー0を返す")
    void getMonthlySummary_noData_returnsZeros() {
        stubAttendances(Collections.emptyList());

        var result = service.getMonthlySummary(EMPLOYEE_ID, YEAR, MONTH);

        assertThat(result.totalWorkingMinutes()).isEqualTo(0);
        assertThat(result.workingDays()).isEqualTo(0);
    }

    @Test
    @DisplayName("部門メンバーの月次勤怠を取得できる")
    void getDepartmentMonthlyAttendance_returnsPerMember() {
        Long departmentId = 10L;
        var members = List.of(
                buildMembership(1L, departmentId),
                buildMembership(2L, departmentId));
        when(employeeDepartmentRepository.findByDepartmentIdAndEndDateIsNull(departmentId))
                .thenReturn(members);

        var attendances1 = List.of(buildAttendance(LocalDate.of(2026, 7, 1), 450, 0, 0, false));
        var attendances2 = List.of(buildAttendance(LocalDate.of(2026, 7, 1), 480, 30, 0, false));

        when(dailyAttendanceRepository.findByEmployeeIdsAndMonth(
                eq(List.of(1L, 2L)), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(
                        buildAttendanceWithEmployee(1L, LocalDate.of(2026, 7, 1), 450, 0, 0, false),
                        buildAttendanceWithEmployee(2L, LocalDate.of(2026, 7, 1), 480, 30, 0, false)));

        var result = service.getDepartmentMonthlyAttendance(departmentId, YEAR, MONTH);

        assertThat(result).hasSize(2);
    }

    private DailyAttendance buildAttendance(LocalDate date, int working, int overtime, int night, boolean holiday) {
        return buildAttendanceWithEmployee(EMPLOYEE_ID, date, working, overtime, night, holiday);
    }

    private DailyAttendance buildAttendanceWithEmployee(Long empId, LocalDate date, int working, int overtime, int night, boolean holiday) {
        return DailyAttendance.builder()
                .employeeId(empId)
                .attendanceDate(date)
                .clockIn(OffsetDateTime.of(date.atTime(9, 0), JST))
                .clockOut(OffsetDateTime.of(date.atTime(18, 0), JST))
                .breakMinutes(60)
                .workingMinutes(working)
                .overtimeMinutes(overtime)
                .nightMinutes(night)
                .holidayWork(holiday)
                .status(AttendanceStatus.PRESENT)
                .build();
    }

    private void stubAttendances(List<DailyAttendance> attendances) {
        when(dailyAttendanceRepository.findByEmployeeIdAndMonth(
                eq(EMPLOYEE_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(attendances);
    }

    private EmployeeDepartment buildMembership(Long employeeId, Long departmentId) {
        return EmployeeDepartment.builder()
                .employeeId(employeeId)
                .departmentId(departmentId)
                .primary(true)
                .startDate(LocalDate.of(2020, 4, 1))
                .build();
    }
}
